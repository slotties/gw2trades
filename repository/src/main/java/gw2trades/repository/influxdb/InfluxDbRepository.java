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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * This repository implementation writes data into an Influx DB. Every item receives its own series.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbRepository implements ItemRepository {
    private static final Logger LOGGER = LogManager.getLogger(InfluxDbRepository.class);

    private static final String INFLUX_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private static final String INFLUX_QUERY_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
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

            // FIXME: respect listing costs (5%) and selling costs (10%)
            int profit = sells.getMinPrice() - buys.getMaxPrice();

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

    @Override
    public List<ListingStatistics> getHistory(int itemId, long fromTimestamp, long toTimestamp) throws IOException {
        SimpleDateFormat influxDateFormat = new SimpleDateFormat(INFLUX_QUERY_DATE_FORMAT);

        InfluxDB influxDB = connectionManager.getConnection();
        org.influxdb.dto.Query query = new org.influxdb.dto.Query(
                "select time, " +
                        " buys_avg, buys_max, buys_min, buys_total," +
                        " sells_avg, sells_max, sells_min, sells_total," +
                        " profit" +
                        " from item_" + itemId +
                        " where time >= '" + influxDateFormat.format(new Date(fromTimestamp)) + "'" +
                                " and time <= '" + influxDateFormat.format(new Date(toTimestamp)) + "'",
                "gw2trades"
        );
        QueryResult results = influxDB.query(query);
        List<ListingStatistics> allStats = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat(INFLUX_DATE_FORMAT);

        for (QueryResult.Result result : results.getResults()) {
            if (result.getSeries() == null) {
                continue;
            }

            for (QueryResult.Series series : result.getSeries()) {
                List<List<Object>> values = series.getValues();
                List<ListingStatistics> seriesStats = values.stream()
                        .map(obj -> {
                            PriceStatistics buys = new PriceStatistics();
                            PriceStatistics sells = new PriceStatistics();
                            ListingStatistics stats = new ListingStatistics();
                            stats.setBuyStatistics(buys);
                            stats.setSellStatistics(sells);

                            stats.setItemId(itemId);
                            try {
                                stats.setTimestamp(dateFormat.parse((String) obj.get(0)).getTime());
                            } catch (ParseException e) {
                                LOGGER.warn("Could not parse date string {}", obj.get(0), e);
                            }

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
        //doc.add(new DoubleField("profit_score", profitScore, DOUBLE_FIELD_TYPE_STORED_SORTED));

        return doc;
    }

    private double calculateProfitScore(int profit, PriceStatistics sells) {
        // 30% profit is our goal, the closer we are to that goal the higher is the profit score.
        double goal = 0.3;
        double relativeProfit = ((double) profit / (double) sells.getMinPrice());
        double profitScore;
        /*
            We have a ladder score:
            0.0 to (goal * 2) = highest rating based on the distance to the goal itself.
            (goal * 2) to (goal * 10) = next best rating, because these are interesting
             > (goal * 10) = not interesting, because they're not realistic
             <0 = not interesting, because we lose money here
         */
        if (relativeProfit >= 0.0 && relativeProfit <= (goal * 2.0)) {
            profitScore = 1.0 - goal - relativeProfit;
        } else if (relativeProfit >= (goal * 10.0)) {
            profitScore = 0.5;
        } else if (relativeProfit >= (goal * 2.0)){
            profitScore = 0.6;
        } else {
            profitScore = 0.4;
        }

        return profitScore;
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
