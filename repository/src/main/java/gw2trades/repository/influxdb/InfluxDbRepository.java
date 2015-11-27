package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
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
    private InfluxDbConnectionManager connectionManager;

    private String indexDir;
    private IndexReader indexReader;

    private FieldType textField;

    public InfluxDbRepository(InfluxDbConnectionManager connectionManager, String indexDir) throws IOException {
        this.connectionManager = connectionManager;

        textField = StringField.TYPE_STORED;

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

            Point buysPoint = createPoint(listing.getItemId(), "buys", buys);
            Point sellsPoint = createPoint(listing.getItemId(), "sells", sells);
            points.point(buysPoint);
            points.point(sellsPoint);

            Document doc = createStatsDoc(listing.getItem(), buys, sells);
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
    public Collection<ListingStatistics> listStatistics(Order order, int fromPage, int toPage) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);

        MatchAllDocsQuery query = new MatchAllDocsQuery();
        Sort sort = createSort(order);

        TopDocs docs = sort != null ? searcher.search(query, Integer.MAX_VALUE, sort) : searcher.search(query, Integer.MAX_VALUE);
        Collection<ListingStatistics> allStats = new ArrayList<>();
        // TODO: paging
        for (ScoreDoc scoreDoc : docs.scoreDocs) {
            Document doc = searcher.doc(scoreDoc.doc);
            ListingStatistics stats = toStatistics(doc);

            allStats.add(stats);
        }

        return allStats;
    }

    private Sort createSort(Order order) {
        if (order == null) {
            return null;
        }

        String luceneField;
        SortField.Type fieldType;

        switch (order.getField()) {
            case "name":
                luceneField = "name";
                fieldType = SortField.Type.STRING;
                break;
            case "highestBidder":
                luceneField = "buys_max";
                fieldType = SortField.Type.INT;
                break;
            case "lowestSeller":
                luceneField = "sells_min";
                fieldType = SortField.Type.INT;
                break;
            default:
                luceneField = null;
                fieldType = null;
                break;
            // TODO: averages
        }

        if (luceneField == null) {
            return null;
        }

        return new Sort(new SortField(luceneField, fieldType, order.isDescending()));
    }

    @Override
    public Collection<ListingStatistics> queryStatistics(Query query) throws IOException {
        return null;
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
        // TODO: avg

        PriceStatistics sells = new PriceStatistics();
        sells.setMinPrice(Integer.valueOf(doc.get("sells_min")));
        sells.setMaxPrice(Integer.valueOf(doc.get("sells_max")));
        sells.setTotalAmount(Integer.valueOf(doc.get("sells_total")));

        stats.setBuyStatistics(buys);
        stats.setSellStatistics(sells);

        return stats;
    }

    private Document createStatsDoc(Item item, PriceStatistics buys, PriceStatistics sells) {
        Document doc = new Document();
        doc.add(new Field("name", item.getName(), textField));
        doc.add(new SortedDocValuesField("name", new BytesRef(item.getName())));

        doc.add(new Field("iconUrl", item.getIconUrl(), textField));
        doc.add(new IntField("level", item.getLevel(), IntField.TYPE_STORED));
        doc.add(new IntField("itemId", item.getItemId(), IntField.TYPE_STORED));

        // TODO: average

        doc.add(new IntField("buys_min", buys.getMinPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_min", buys.getMinPrice()));
        doc.add(new IntField("buys_max", buys.getMaxPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_max", buys.getMinPrice()));
        doc.add(new IntField("buys_total", buys.getTotalAmount(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("buys_total", buys.getMinPrice()));

        doc.add(new IntField("sells_min", sells.getMinPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_min", buys.getMinPrice()));
        doc.add(new IntField("sells_max", sells.getMaxPrice(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_max", buys.getMinPrice()));
        doc.add(new IntField("sells_total", sells.getTotalAmount(), IntField.TYPE_STORED));
        doc.add(new NumericDocValuesField("sells_total", buys.getMinPrice()));

        return doc;
    }

    private Point createPoint(int itemId, String type, PriceStatistics stats) {
        return Point.measurement("item_" + itemId + "_" + type)
                .field("minPrice", stats.getMinPrice())
                .field("maxPrice", stats.getMaxPrice())
                .field("avgPrice", stats.getAverage())
                .field("totalItems", stats.getTotalAmount())
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
