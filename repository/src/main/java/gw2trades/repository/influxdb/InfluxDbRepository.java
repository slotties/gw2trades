package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;
import org.influxdb.dto.QueryResult;

import java.io.IOException;
import java.nio.file.Paths;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This repository implementation writes data into an Influx DB. Every item receives its own series.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbRepository implements ItemRepository {
    private static final Logger LOGGER = LogManager.getLogger(InfluxDbRepository.class);

    private static final DateTimeFormatter INFLUX_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
    private static final DateTimeFormatter INFLUX_QUERY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private static final FieldType DOUBLE_FIELD_TYPE_STORED_SORTED = new FieldType();

    static {
        DOUBLE_FIELD_TYPE_STORED_SORTED.setTokenized(true);
        DOUBLE_FIELD_TYPE_STORED_SORTED.setOmitNorms(true);
        DOUBLE_FIELD_TYPE_STORED_SORTED.setIndexOptions(IndexOptions.DOCS);
        DOUBLE_FIELD_TYPE_STORED_SORTED.setNumericType(FieldType.NumericType.DOUBLE);
        DOUBLE_FIELD_TYPE_STORED_SORTED.setStored(true);
        DOUBLE_FIELD_TYPE_STORED_SORTED.setDocValuesType(DocValuesType.NUMERIC);
        DOUBLE_FIELD_TYPE_STORED_SORTED.freeze();
    }

    private InfluxDbConnectionManager connectionManager;

    private String indexDir;
    private IndexReader indexReader;

    public InfluxDbRepository(InfluxDbConnectionManager connectionManager, String indexDir, boolean enableReading) throws IOException {
        this.connectionManager = connectionManager;
        this.indexDir = indexDir;
        if (enableReading) {
            this.indexReader = openIndexReader();
        }
    }

    private IndexWriter openIndexWriter() throws IOException {
        // TODO: correct analyzers
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        return new IndexWriter(directory, iwc);
    }

    @Override
    public void store(Collection<ItemListings> listings, long timestamp) throws IOException {
        BatchPoints points = BatchPoints
                .database("gw2trades")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        IndexWriter indexWriter = openIndexWriter();
        for (ItemListings listing : listings) {
            PriceStatistics buys = createStatistics(listing.getBuys());
            PriceStatistics sells = createStatistics(listing.getSells());

            int fixCosts = (int) Math.floor(((float) sells.getMinPrice()) * 0.15f);
            int profit = (sells.getMinPrice() - buys.getMaxPrice()) - fixCosts;

            Point dataPoint = createPoint(listing.getItem(), buys, sells, profit);
            points.point(dataPoint);

            Document doc = createStatsDoc(listing.getItem(), buys, sells, profit);
            indexWriter.addDocument(doc);
        }

        InfluxDB influxDB = connectionManager.getConnection();
        try {
            influxDB.write(points);
            indexWriter.commit();
        } finally {
            indexWriter.close();
        }
    }

    @Override
    public SearchResult<ListingStatistics> listStatistics(Query query, Order order, int fromIdx, int toIdx) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);

        org.apache.lucene.search.Query luceneQuery = createLuceneQuery(query);
        Sort sort = createSort(order);
        if (sort == null) {
            sort = defaultSort();
        }

        TopDocs docs = searcher.search(luceneQuery, Integer.MAX_VALUE, sort);
        List<ListingStatistics> allStats = new ArrayList<>();
        for (int i = fromIdx; i < toIdx && i < docs.scoreDocs.length; i++) {
            Document doc = searcher.doc(docs.scoreDocs[i].doc);
            ListingStatistics stats = toStatistics(doc);

            allStats.add(stats);
        }

        return new SearchResult<>(allStats, docs.totalHits);
    }

    private org.apache.lucene.search.Query createLuceneQuery(Query query) {
        org.apache.lucene.search.Query luceneQuery = null;
        if (query != null) {
            if (query.getName() != null && !query.getName().trim().isEmpty()) {
                String[] words = query.getName().split(" ");
                BooleanQuery.Builder booleanQuery = new BooleanQuery.Builder();
                for (String word : words) {
                    booleanQuery.add(new TermQuery(new Term("name", word)), BooleanClause.Occur.SHOULD);
                }

                luceneQuery = booleanQuery.build();
            }
        }

        if (luceneQuery == null) {
            luceneQuery = new MatchAllDocsQuery();
        }

        return luceneQuery;
    }

    private Sort defaultSort() {
        return new Sort(
                new SortedNumericSortField("profit_score", SortField.Type.DOUBLE, true),
                new SortedNumericSortField("profit", SortField.Type.INT, true)
        );
    }

    private Sort createSort(Order order) {
        if (order == null) {
            return null;
        }

        SortField sortField;

        switch (order.getField()) {
            case "name":
                sortField = new SortField("name", SortField.Type.STRING, order.isDescending());
                break;
            case "highestBidder":
                sortField = new SortedNumericSortField("buys_max", SortField.Type.INT, !order.isDescending());
                break;
            case "lowestSeller":
                sortField = new SortedNumericSortField("sells_min", SortField.Type.INT, !order.isDescending());
                break;
            case "avgBidder":
                sortField = new SortedNumericSortField("buys_avg", SortField.Type.DOUBLE, !order.isDescending());
                break;
            case "avgSeller":
                sortField = new SortedNumericSortField("sells_avg", SortField.Type.DOUBLE, !order.isDescending());
                break;
            case "profit":
                sortField = new SortedNumericSortField("profit", SortField.Type.INT, !order.isDescending());
                break;
            default:
                sortField = null;
                break;
        }

        if (sortField == null) {
            return null;
        }

        return new Sort(sortField);
    }

    private String createInfluxQuery(int itemId, LocalDateTime fromDate, LocalDateTime toDate) {
        // Stretch the dates a little bit to avoid single-data-points and cut-off lines.
        fromDate = fromDate.minus(1, ChronoUnit.DAYS);
        toDate = toDate.plus(1, ChronoUnit.DAYS);
        String fromTime = INFLUX_QUERY_DATE_FORMAT.format(fromDate);
        String toTime = INFLUX_QUERY_DATE_FORMAT.format(toDate);

        Duration duration = Duration.between(fromDate, toDate);
        long days = duration.toDays();
        if (days < 4) {
            // Display all points without grouping them.
            return "select time, " +
                    " buys_avg, buys_max, buys_min, buys_total," +
                    " sells_avg, sells_max, sells_min, sells_total," +
                    " profit" +
                    " from item_" + itemId +
                    " where time >= '" + fromTime + "'" +
                    " and time <= '" + toTime + "'";
        }

        String groupByDuration;
        if (days < 7) {
            groupByDuration = "1h";
        } else if (days < 14) {
            groupByDuration = "4h";
        } else if (days < 30) {
            groupByDuration = "8h";
        } else {
            groupByDuration = "1d";
        }

        return "select " +
                " mean(buys_avg), max(buys_max), min(buys_min), sum(buys_total)," +
                " mean(sells_avg), max(sells_max), min(sells_min), sum(sells_total)," +
                " min(profit)" +
                " from item_" + itemId +
                " where time >= '" + fromTime + "'" +
                " and time <= '" + toTime + "'" +
                " group by time(" + groupByDuration + ")";
    }

    @Override
    public List<ListingStatistics> getHistory(int itemId, LocalDateTime fromTime, LocalDateTime toTime) throws IOException {
        InfluxDB influxDB = connectionManager.getConnection();
        String queryStr = createInfluxQuery(itemId, fromTime, toTime);
        org.influxdb.dto.Query query = new org.influxdb.dto.Query(queryStr, "gw2trades");
        QueryResult results = influxDB.query(query);
        List<ListingStatistics> allStats = new ArrayList<>();

        for (QueryResult.Result result : results.getResults()) {
            if (result.getSeries() == null) {
                continue;
            }

            for (QueryResult.Series series : result.getSeries()) {
                List<List<Object>> values = series.getValues();
                List<ListingStatistics> seriesStats = values.stream()
                        .filter(obj -> obj.size() > 1 && obj.get(1) != null)
                        .map(obj -> {
                            PriceStatistics buys = new PriceStatistics();
                            PriceStatistics sells = new PriceStatistics();
                            ListingStatistics stats = new ListingStatistics();
                            stats.setBuyStatistics(buys);
                            stats.setSellStatistics(sells);

                            stats.setItemId(itemId);
                            stats.setTimestamp(parseDateString((String) obj.get(0), INFLUX_DATE_FORMAT));

                            buys.setAverage(influxDouble(obj.get(1)));
                            buys.setMaxPrice(influxInt(obj.get(2)));
                            buys.setMinPrice(influxInt(obj.get(3)));
                            buys.setTotalAmount(influxInt(obj.get(4)));

                            sells.setAverage(influxDouble(obj.get(5)));
                            sells.setMaxPrice(influxInt(obj.get(6)));
                            sells.setMinPrice(influxInt(obj.get(7)));
                            sells.setTotalAmount(influxInt(obj.get(8)));

                            stats.setProfit(influxInt(obj.get(9)));

                            return stats;
                        })
                        .collect(Collectors.toList());

                allStats.addAll(seriesStats);
            }
        }

        return allStats;
    }

    private long parseDateString(String str, DateTimeFormatter parser) {
        int msecDelim = str.lastIndexOf('.');
        if (msecDelim > 0) {
            // Cut off msecs
            str = str.substring(0, str.lastIndexOf('.'));
        } else {
            // Cut off the 'Z' at the end.
            str = str.substring(0, str.length() - 1);
        }
        try {
            LocalDateTime ldt = LocalDateTime.parse(str, parser);
            return ldt.toInstant(ZoneOffset.UTC).toEpochMilli();
        } catch (DateTimeParseException e) {
            // ignore, try next parser.

            LOGGER.warn("Could not parse date string {}", str);
            return 0;
        }
    }

    private double influxDouble(Object v) {
        return ((Number) v).doubleValue();
    }

    private int influxInt(Object v) {
        return ((Number) v).intValue();
    }

    @Override
    public ListingStatistics latestStatistics(int itemId) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);

        TermQuery query = new TermQuery(new Term("itemId", Integer.toString(itemId)));
        TopDocs docs = searcher.search(query, 1);
        if (docs.totalHits < 1) {
            return null;
        }

        Document doc = searcher.doc(docs.scoreDocs[0].doc);
        return toStatistics(doc);
    }

    @Override
    public void close() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
    }

    @Override
    public void reopen() throws IOException {
        close();
        this.indexReader = openIndexReader();
    }

    private IndexReader openIndexReader() throws IOException {
        return DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
    }

    private ListingStatistics toStatistics(Document doc) {
        ListingStatistics stats = new ListingStatistics();
        stats.setItemId(Integer.valueOf(doc.get("itemId")));

        Item item = new Item();
        item.setItemId(stats.getItemId());
        item.setLevel(Integer.valueOf(doc.get("level")));
        item.setIconUrl(doc.get("iconUrl"));
        item.setName(doc.get("name"));
        item.setRarity(doc.get("rarity"));
        stats.setItem(item);

        PriceStatistics buys = new PriceStatistics();
        buys.setMinPrice(Integer.valueOf(doc.get("buys_min")));
        buys.setMaxPrice(Integer.valueOf(doc.get("buys_max")));
        buys.setTotalAmount(Integer.valueOf(doc.get("buys_total")));
        buys.setAverage(Double.valueOf(doc.get("buys_avg")));

        PriceStatistics sells = new PriceStatistics();
        sells.setMinPrice(Integer.valueOf(doc.get("sells_min")));
        sells.setMaxPrice(Integer.valueOf(doc.get("sells_max")));
        sells.setTotalAmount(Integer.valueOf(doc.get("sells_total")));
        sells.setAverage(Double.valueOf(doc.get("sells_avg")));

        stats.setBuyStatistics(buys);
        stats.setSellStatistics(sells);

        stats.setProfit(Integer.valueOf(doc.get("profit")));

        return stats;
    }

    private Document createStatsDoc(Item item, PriceStatistics buys, PriceStatistics sells, int profit) {
        Document doc = new Document();
        doc.add(new TextField("name", item.getName(), Field.Store.YES));
        doc.add(new SortedDocValuesField("name", new BytesRef(item.getName())));

        doc.add(new TextField("rarity", item.getRarity(), Field.Store.YES));
        doc.add(new SortedDocValuesField("rarity", new BytesRef(item.getRarity())));

        doc.add(new StringField("iconUrl", item.getIconUrl(), Field.Store.YES));
        doc.add(new IntField("level", item.getLevel(), IntField.TYPE_STORED));
        doc.add(new StringField("itemId", Integer.toString(item.getItemId()), Field.Store.YES));

        doc.add(new IntField("buys_min", buys.getMinPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_min", buys.getMinPrice()));
        doc.add(new IntField("buys_max", buys.getMaxPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_max", buys.getMaxPrice()));
        doc.add(new IntField("buys_total", buys.getTotalAmount(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_total", buys.getTotalAmount()));
        doc.add(new DoubleField("buys_avg", buys.getAverage(), DOUBLE_FIELD_TYPE_STORED_SORTED));

        doc.add(new IntField("sells_min", sells.getMinPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_min", sells.getMinPrice()));
        doc.add(new IntField("sells_max", sells.getMaxPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_max", sells.getMaxPrice()));
        doc.add(new IntField("sells_total", sells.getTotalAmount(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_total", sells.getTotalAmount()));
        doc.add(new DoubleField("sells_avg", sells.getAverage(), DOUBLE_FIELD_TYPE_STORED_SORTED));

        doc.add(new IntField("profit", profit, IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("profit", profit));

        double profitScore = calculateProfitScore(profit, sells);
        doc.add(new DoubleField("profit_score", profitScore, DoubleField.TYPE_STORED));
        doc.add(new DoubleDocValuesField("profit_score", profitScore));

        LOGGER.info("Indexed {} with profit_score={} having sells_min={} and profit={}", item.getItemId(), profitScore, sells.getMinPrice(), profit);

        return doc;
    }

    private double calculateProfitScore(int profit, PriceStatistics sells) {
        /*
            The algorithm is:
            - the closer the revenue is to 50% the higher it ranks. The score is distributed between 0.5 and 1.0.
            - profits >100% are not realistic. These are listed in 0.25 and 0.49.
            - negative profits are scored with 0.0 to 0.1.
         */

        double goal = 0.5;
        double revenue = ((double) profit / (double) sells.getMinPrice());
        double distanceToGoal = (goal - revenue) * 10.0;
        double profitScore = (-(Math.pow(distanceToGoal, 2)) + 100.0) / 100.0;

        return Math.max(0.0, profitScore);
    }

    private Point createPoint(Item item, PriceStatistics buys, PriceStatistics sells, int profit) {
        return Point.measurement("item_" + item.getItemId())

                .field("buys_min", buys.getMinPrice())
                .field("buys_max", buys.getMaxPrice())
                .field("buys_avg", buys.getAverage())
                .field("buys_total", buys.getTotalAmount())

                .field("sells_min", sells.getMinPrice())
                .field("sells_max", sells.getMaxPrice())
                .field("sells_avg", sells.getAverage())
                .field("sells_total", sells.getTotalAmount())

                .field("profit", profit)

                .build();
    }

    private PriceStatistics createStatistics(List<ItemListing> listings) {
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;
        int priceTotal = 0;
        int amountOfPrices = 0;

        for (ItemListing listing : listings) {
            minPrice = Math.min(minPrice, listing.getUnitPrice());
            maxPrice = Math.max(maxPrice, listing.getUnitPrice());
            priceTotal += listing.getUnitPrice() * listing.getQuantity();
            amountOfPrices += listing.getQuantity();
        }

        int avgPrice = (amountOfPrices == 0) ? 0 : (priceTotal / amountOfPrices);
        if (minPrice == Integer.MAX_VALUE) {
            minPrice = 0;
        }

        PriceStatistics stats = new PriceStatistics();
        stats.setAverage(avgPrice);
        stats.setMaxPrice(maxPrice);
        stats.setMinPrice(minPrice);
        stats.setTotalAmount(amountOfPrices);

        return stats;
    }
}
