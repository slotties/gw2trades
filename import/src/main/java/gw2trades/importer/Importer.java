package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.importer.model.CommerceListing;
import gw2trades.importer.model.CommerceListings;
import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.BatchPoints;
import org.influxdb.dto.Point;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Importer {
    private TradingPost tradingPost;
    private Config config;
    private String databaseName;

    public Importer(Config config, TradingPost tradingPost) {
        this.config = config;
        this.tradingPost = tradingPost;

        this.databaseName = config.required("influxdb", "dbName");
    }

    public void execute() throws IOException {
        InfluxDB influxDb = connectToInflux();
        setupDatabase(influxDb);

        List<Integer> itemIds = tradingPost.listItemIds();

        int batchSize = 50;
        int batches = (int) Math.ceil((float) itemIds.size() / (float) batchSize);
        for (int i = 0; i < batches; i++) {
            System.out.printf("Pulling batch %d of %d (total %d items)...\n",
                    i, batches, itemIds.size());

            // TODO: implement parallel processing to improve speed
            List<Integer> itemIdBatch = itemIds.subList(i * batchSize, Math.min(itemIds.size() - 1, (i + 1) * batchSize));
            List<CommerceListings> listings = tradingPost.listings(itemIdBatch);

            BatchPoints points = BatchPoints
                    .database("gw2trades")
                            // TODO: read doc
                    .retentionPolicy("default")
                            // TODO: read doc
                    .consistency(InfluxDB.ConsistencyLevel.ALL)
                    .build();

            for (CommerceListings listing : listings) {
                System.out.printf("%d: %d buyers, %d sellers\n",
                        listing.getItemId(),
                        listing.getBuys().size(), listing.getSells().size());

                Point buys = createPoint(listing.getItemId(), "buys", listing.getBuys());
                Point sells = createPoint(listing.getItemId(), "sells", listing.getBuys());
                points.point(buys);
                points.point(sells);
            }

            influxDb.write(points);
        }
    }

    private Point createPoint(int itemId, String type, List<CommerceListing> listings) {
        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;
        int priceTotal = 0;
        int amountOfPrices = 0;

        // TODO: percentiles
        for (CommerceListing listing : listings) {
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

    private void setupDatabase(InfluxDB influxDb) {
        influxDb.deleteDatabase("gw2trades");
        // FIXME: check if database exists
        influxDb.createDatabase("gw2trades");
    }

    private InfluxDB connectToInflux() {
        return InfluxDBFactory.connect(
                config.required("influxdb", "url"),
                config.optional("influxdb", "user").orElse(""),
                config.optional("influxdb", "pass").orElse("")
        );
    }
}
