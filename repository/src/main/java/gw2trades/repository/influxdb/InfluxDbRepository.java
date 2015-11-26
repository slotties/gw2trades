package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListing;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.api.model.ListingStatistics;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * This repository implementation writes data into an Influx DB. Every item receives its own series.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbRepository implements ItemRepository {
    private InfluxDbConnectionManager connectionManager;

    public InfluxDbRepository(InfluxDbConnectionManager connectionManager) {
        this.connectionManager = connectionManager;
    }

    @Override
    public void store(Collection<ItemListings> listings, long timestamp) {
        BatchPoints points = BatchPoints
                .database("gw2trades")
                .retentionPolicy("default")
                .consistency(InfluxDB.ConsistencyLevel.ALL)
                .build();

        for (ItemListings listing : listings) {
            Point buys = createPoint(listing.getItemId(), "buys", listing.getBuys());
            Point sells = createPoint(listing.getItemId(), "sells", listing.getBuys());
            points.point(buys);
            points.point(sells);
        }

        InfluxDB influxDB = connectionManager.getConnection();
        influxDB.write(points);
    }

    @Override
    public void store(Collection<Item> items) throws IOException {
        // TODO
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

    private Point createPoint(int itemId, String type, List<ItemListing> listings) {
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

        return Point.measurement("item_" + itemId + "_" + type)
                .field("minPrice", minPrice)
                .field("maxPrice", maxPrice)
                .field("avgPrice", avgPrice)
                .field("totalItems", amountOfPrices)
                .build();
    }
}
