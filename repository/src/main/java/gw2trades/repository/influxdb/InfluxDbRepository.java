package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.*;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.index.IndexOptions;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

/**
 * This repository implementation writes data into an Influx DB. Every item receives its own series.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbRepository implements ItemRepository {
    private InfluxDbConnectionManager connectionManager;

    private IndexWriter itemIndexWriter;
    private IndexWriter latestStatisticsIndexWriter;

    private FieldType textField;
    private FieldType numberField;

    public InfluxDbRepository(InfluxDbConnectionManager connectionManager, String indexDir) throws IOException {
        this.connectionManager = connectionManager;

        textField = new FieldType();
        textField.setStored(true);
        textField.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
        textField.setTokenized(true);

        numberField = new FieldType();
        numberField.setStored(true);
        numberField.setNumericType(FieldType.NumericType.INT);

        openItemIndex(indexDir);
        openStatisticsIndex(indexDir);
    }

    private void openItemIndex(String indexDir) throws IOException {
        // TODO: correct analyzers
        Directory directory = FSDirectory.open(Paths.get(indexDir, "items"));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        this.itemIndexWriter = new IndexWriter(directory, iwc);
    }

    private void openStatisticsIndex(String indexDir) throws IOException {
        // TODO: correct analyzers
        Directory directory = FSDirectory.open(Paths.get(indexDir, "statistics"));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        this.latestStatisticsIndexWriter = new IndexWriter(directory, iwc);
    }

    @Override
    public void store(Collection<ItemListings> listings, long timestamp) throws IOException {
        BatchPoints points = BatchPoints
                .database("gw2trades")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for (ItemListings listing : listings) {
            PriceStatistics buys = createStatistics(listing.getBuys());
            PriceStatistics sells = createStatistics(listing.getSells());

            Point buysPoint = createPoint(listing.getItemId(), "buys", buys);
            Point sellsPoint = createPoint(listing.getItemId(), "sells", sells);
            points.point(buysPoint);
            points.point(sellsPoint);

            Document doc = createStatsDoc(listing.getItem(), buys, sells);
            this.latestStatisticsIndexWriter.addDocument(doc);
        }

        InfluxDB influxDB = connectionManager.getConnection();
        influxDB.write(points);

        this.latestStatisticsIndexWriter.commit();
    }

    @Override
    public void store(Collection<Item> items) throws IOException {
        for (Item item : items) {
            Document doc = new Document();
            doc.add(new Field("name", item.getName(), textField));
            doc.add(new Field("iconUrl", item.getIconUrl(), textField));
            doc.add(new Field("itemId", Integer.toString(item.getItemId()), numberField));
            doc.add(new Field("level", Integer.toString(item.getLevel()), numberField));
            this.itemIndexWriter.addDocument(doc);
        }

        this.itemIndexWriter.commit();
    }

    @Override
    public Item getItem(int itemId) throws IOException {
        // TODO
        return null;
    }

    @Override
    public Collection<ListingStatistics> listStatistics() throws IOException {
        // TODO
        return null;
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
        this.itemIndexWriter.close();
        this.latestStatisticsIndexWriter.close();
    }

    private Document createStatsDoc(Item item, PriceStatistics buys, PriceStatistics sells) {
        Document doc = new Document();
        doc.add(new Field("name", item.getName(), textField));
        doc.add(new Field("itemId", Integer.toString(item.getItemId()), numberField));
        // TODO: average

        doc.add(new Field("buys_min", Integer.toString(buys.getMinPrice()), numberField));
        doc.add(new Field("buys_max", Integer.toString(buys.getMaxPrice()), numberField));
        doc.add(new Field("buys_total", Integer.toString(buys.getTotalAmount()), numberField));

        doc.add(new Field("sells_min", Integer.toString(sells.getMinPrice()), numberField));
        doc.add(new Field("sells_max", Integer.toString(sells.getMaxPrice()), numberField));
        doc.add(new Field("sells_total", Integer.toString(sells.getTotalAmount()), numberField));

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
