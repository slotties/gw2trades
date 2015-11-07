package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.influxdb.InfluxDbConnectionManager;
import gw2trades.repository.influxdb.InfluxDbRepository;
import org.influxdb.InfluxDB;

import java.io.IOException;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Importer {
    // TODO: use slf4j
    private TradingPost tradingPost;
    private Config config;
    private String databaseName;

    public Importer(Config config, TradingPost tradingPost) {
        this.config = config;
        this.tradingPost = tradingPost;

        this.databaseName = config.required("influxdb", "dbName");
    }

    public void execute() throws IOException {
        InfluxDbConnectionManager connectionManager = new InfluxDbConnectionManager(
                config.required("influxdb", "url"),
                config.optional("influxdb", "user").orElse(""),
                config.optional("influxdb", "pass").orElse("")
        );
        setupDatabase(connectionManager);

        ItemRepository repository = new InfluxDbRepository(connectionManager);

        List<Integer> itemIds = tradingPost.listItemIds();

        int batchSize = 50;
        int batches = (int) Math.ceil((float) itemIds.size() / (float) batchSize);
        for (int i = 0; i < batches; i++) {
            System.out.printf("Pulling batch %d of %d (total %d items)...\n",
                    i, batches, itemIds.size());

            // TODO: implement parallel processing to improve speed
            List<Integer> itemIdBatch = itemIds.subList(i * batchSize, Math.min(itemIds.size() - 1, (i + 1) * batchSize));
            List<ItemListings> listings = tradingPost.listings(itemIdBatch);

            System.out.println("Writing into repository ...");
            repository.store(listings, System.currentTimeMillis());
        }
    }

    private void setupDatabase(InfluxDbConnectionManager influxDbConnectionManager) {
        InfluxDB influxDb = influxDbConnectionManager.getConnection();
        influxDb.deleteDatabase("gw2trades");
        // FIXME: check if database exists
        influxDb.createDatabase("gw2trades");
    }
}
