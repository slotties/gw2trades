package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.filesystem.FilesystemItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Importer {
    private static final Logger LOGGER = LogManager.getLogger(Importer.class);

    private TradingPost tradingPost;
    private Config config;
    private String databaseName;

    public Importer(Config config, TradingPost tradingPost) {
        this.config = config;
        this.tradingPost = tradingPost;

        this.databaseName = config.required("influxdb", "dbName");
    }

    public void execute() throws IOException {
        /*
        InfluxDbConnectionManager connectionManager = new InfluxDbConnectionManager(
                config.required("influxdb", "url"),
                config.optional("influxdb", "user").orElse(""),
                config.optional("influxdb", "pass").orElse("")
        );
        setupDatabase(connectionManager);

        ItemRepository repository = new InfluxDbRepository(connectionManager);
        */

        File dataDir = new File(config.required("filesystem", "dir"));
        LOGGER.info("Importing into {} ...\n", dataDir.getAbsolutePath());
        ItemRepository repository = new FilesystemItemRepository(dataDir);

        List<Integer> itemIds = tradingPost.listItemIds();

        int batchSize = 50;
        int batches = (int) Math.ceil((float) itemIds.size() / (float) batchSize);
        for (int i = 0; i < batches; i++) {
            LOGGER.info("Pulling batch {} of {} (total {} items)...", i, batches, itemIds.size());
            // TODO: implement parallel processing to improve speed
            List<Integer> itemIdBatch = itemIds.subList(i * batchSize, Math.min(itemIds.size() - 1, (i + 1) * batchSize));

            List<ItemListings> listings = tradingPost.listings(itemIdBatch);
            List<Item> items = tradingPost.listItems(itemIdBatch);

            LOGGER.info("Writing listings into repository ...");
            repository.store(listings, System.currentTimeMillis());

            LOGGER.info("Writing items into repository ...");
            repository.store(items);
        }
    }
/*
    private void setupDatabase(InfluxDbConnectionManager influxDbConnectionManager) {
        InfluxDB influxDb = influxDbConnectionManager.getConnection();
        influxDb.deleteDatabase("gw2trades");
        // FIXME: check if database exists
        influxDb.createDatabase("gw2trades");
    }
*/
}
