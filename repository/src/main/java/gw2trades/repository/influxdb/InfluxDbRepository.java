package gw2trades.repository.influxdb;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ItemListing;
import gw2trades.repository.api.model.ItemListings;
import org.influxdb.InfluxDB;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.util.Collection;
import java.util.List;

/**
 * TODO
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
                        // TODO: read doc
                .retentionPolicy("default")
                        // TODO: read doc
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

    private Point createPoint(int itemId, String type, List<ItemListing> listings) {
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;
        int priceTotal = 0;
        int amountOfPrices = 0;

        // TODO: percentiles
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
