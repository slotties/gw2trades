package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.influxdb.InfluxDbConnectionManager;
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
        InfluxDbConnectionManager connectionManager = new InfluxDbConnectionManager(
                config.required("influxdb.url"),
                config.optional("influxdb.user").orElse(""),
                config.optional("influxdb.pass").orElse("")
        );
        setupDatabase(connectionManager);

        String indexDir = config.required("index.dir");

        ItemRepository repository = new InfluxDbRepository(connectionManager, indexDir, false);

        int chunkSize = Integer.valueOf(config.required("importer.chunkSize"));
        int threadCount = Integer.valueOf(config.required("importer.threads"));

        LOGGER.info("Importing with {} threads (each {} chunks)...\n", threadCount, chunkSize);

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        Map<Integer, Item> allItems = new ConcurrentHashMap<>();
        Map<Integer, ItemListings> allListings = new ConcurrentHashMap<>();

        long t0 = System.currentTimeMillis();
        List<Integer> itemIds = tradingPost.listItemIds();
        for (int i = 0; i < itemIds.size(); i += chunkSize) {
            List<Integer> chunk = itemIds.subList(i, Math.min(itemIds.size(), i + chunkSize));
            int chunkNumber = i / chunkSize;
            executorService.execute(() -> {
                try {
                    LOGGER.info("Pulling chunk #{} ...", chunkNumber);
                    List<ItemListings> listings = tradingPost.listings(chunk);
                    List<Item> items = tradingPost.listItems(chunk);

                    for (Item item : items) {
                        allItems.put(item.getItemId(), item);
                    }
                    for (ItemListings listing : listings) {
                        Item item = allItems.get(listing.getItemId());
                        if (item != null) {
                            listing.setItem(item);
                            allListings.put(listing.getItemId(), listing);
                        } else {
                            LOGGER.warn("Could not find item {}.", listing.getItemId());
                        }
                    }
                } catch (IOException e) {
                    LOGGER.error("Could not import item ids {}", chunk, e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        LOGGER.info("Writing everything into repository ...");
        repository.store(allListings.values(), System.currentTimeMillis());

        repository.close();

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
}
