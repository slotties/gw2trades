package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.*;
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

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * This repository implementation writes data into an Influx DB. Every item receives its own series.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbRepository implements ItemRepository {
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

    public InfluxDbRepository(InfluxDbConnectionManager connectionManager, String indexDir) throws IOException {
        this.connectionManager = connectionManager;
        this.indexDir = indexDir;
        this.indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
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

            int profit = sells.getMinPrice() - buys.getMaxPrice();

            Point dataPoint = createPoint(listing.getItem(), buys, sells, profit);
            points.point(dataPoint);

            Document doc = createStatsDoc(listing.getItem(), buys, sells, profit);
            indexWriter.addDocument(doc);
        }

        InfluxDB influxDB = connectionManager.getConnection();
        influxDB.write(points);

        indexWriter.commit();
        indexWriter.close();
    }

    @Override
    public Item getItem(int itemId) throws IOException {
        // TODO
        return null;
    }

    @Override
    public SearchResult<ListingStatistics> listStatistics(Query query, Order order, int fromIdx, int toIdx) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);

        org.apache.lucene.search.Query luceneQuery = createLuceneQuery(query);
        Sort sort = createSort(order);

        TopDocs docs = sort != null ? searcher.search(luceneQuery, Integer.MAX_VALUE, sort) : searcher.search(luceneQuery, Integer.MAX_VALUE);
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
        // TODO
        return null;
    }

    @Override
    public ListingStatistics latestStatistics(int itemId) throws IOException {
        // TODO
        return null;
    }

    @Override
    public void close() throws IOException {
        indexReader.close();
    }

    private ListingStatistics toStatistics(Document doc) {
        ListingStatistics stats = new ListingStatistics();
        stats.setItemId(Integer.valueOf(doc.get("itemId")));

        Item item = new Item();
        item.setItemId(stats.getItemId());
        item.setLevel(Integer.valueOf(doc.get("level")));
        item.setIconUrl(doc.get("iconUrl"));
        item.setName(doc.get("name"));
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

        doc.add(new StringField("iconUrl", item.getIconUrl(), Field.Store.YES));
        doc.add(new IntField("level", item.getLevel(), IntField.TYPE_STORED));
        doc.add(new IntField("itemId", item.getItemId(), IntField.TYPE_STORED));

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

        return doc;
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

        PriceStatistics stats = new PriceStatistics();
        stats.setAverage(avgPrice);
        stats.setMaxPrice(maxPrice);
        stats.setMinPrice(minPrice);
        stats.setTotalAmount(amountOfPrices);

        return stats;
    }
}
