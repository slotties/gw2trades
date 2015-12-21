package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.influxdb.InfluxDbConnectionManager;
import gw2trades.repository.influxdb.InfluxDbConnectionManagerImpl;
import gw2trades.repository.influxdb.InfluxDbRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Importer {
    private static final Logger LOGGER = LogManager.getLogger(Importer.class);

    private TradingPost tradingPost;
    private Config config;

    public Importer(Config config, TradingPost tradingPost) {
        this.config = config;
        this.tradingPost = tradingPost;
    }

    public void execute() throws Exception {
        InfluxDbConnectionManager connectionManager = new InfluxDbConnectionManagerImpl(
                config.required("influxdb.url"),
                config.optional("influxdb.user").orElse(""),
                config.optional("influxdb.pass").orElse("")
        );
        setupDatabase(connectionManager);

        String indexDir = config.required("index.dir");

        int chunkSize = Integer.valueOf(config.required("importer.chunkSize"));
        int threadCount = Integer.valueOf(config.required("importer.threads"));

        LOGGER.info("Importing with {} threads (each {} chunks) into {}...\n", threadCount, chunkSize, indexDir);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        Map<Integer, Item> itemCache = new ConcurrentHashMap<>();
        Map<Integer, ItemListings> allListings = new ConcurrentHashMap<>();

        long t0 = System.currentTimeMillis();
        List<Integer> itemIds = tradingPost.listItemIds();
        for (int i = 0; i < itemIds.size(); i += chunkSize) {
            List<Integer> chunk = itemIds.subList(i, Math.min(itemIds.size(), i + chunkSize));
            executorService.execute(new DataPuller(tradingPost, chunk, itemCache, allListings));
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        if (allListings.isEmpty()) {
            LOGGER.error("The gw2 API did not return any listings. It's broken again. Won't import anything.");
            System.exit(1);
        }

        ItemRepository repository = new InfluxDbRepository(connectionManager, indexDir, false);
        try {
            LOGGER.info("Writing everything into repository ...");
            repository.store(allListings.values(), System.currentTimeMillis());
        } finally {
            repository.close();
        }

        long t1 = System.currentTimeMillis();
        LOGGER.info("Imported the trading post within {} ms.", t1 - t0);
    }

    private void setupDatabase(InfluxDbConnectionManager influxDbConnectionManager) {
        InfluxDB influxDb = influxDbConnectionManager.getConnection();
        try {
            influxDb.createDatabase("gw2trades");
        } catch (Exception e) {
            LOGGER.info("Database exists already.");
        }
    }

    static final class DataPuller implements Runnable {
        private final TradingPost tradingPost;
        private final List<Integer> itemIdsChunk;
        private final Map<Integer, Item> itemCache;
        private final Map<Integer, ItemListings> allListings;

        public DataPuller(TradingPost tradingPost, List<Integer> itemIdsChunk, Map<Integer, Item> itemCache, Map<Integer, ItemListings> allListings) {
            this.tradingPost = tradingPost;
            this.itemIdsChunk = itemIdsChunk;
            this.itemCache = itemCache;
            this.allListings = allListings;
        }

        @Override
        public void run() {
            try {
                List<ItemListings> listings = tradingPost.listings(itemIdsChunk);
                List<Item> items = tradingPost.listItems(itemIdsChunk);

                // Fill the item cache.
                for (Item item : items) {
                    itemCache.put(item.getItemId(), item);
                }

                // Register all listings.
                for (ItemListings listing : listings) {
                    Item item = itemCache.get(listing.getItemId());
                    if (item != null) {
                        listing.setItem(item);
                        allListings.put(listing.getItemId(), listing);
                    } else {
                        LOGGER.warn("Could not find item {}.", listing.getItemId());
                    }
                }
            } catch (IOException e) {
                LOGGER.error("Could not import item ids {}", itemIdsChunk, e);
            }
        }
    }
}
