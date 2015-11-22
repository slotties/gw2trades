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

        File dataDir = new File(config.required("filesystem", "dir"));
        int chunkSize = Integer.valueOf(config.required("importer", "chunkSize"));
        int threadCount = Integer.valueOf(config.required("importer", "threads"));

        LOGGER.info("Importing into {} with {} threads (each {} chunks)...\n", dataDir.getAbsolutePath(), threadCount, chunkSize);

        ItemRepository repository = new FilesystemItemRepository(dataDir);
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

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

                    LOGGER.info("Writing listings into repository ...");
                    repository.store(listings, System.currentTimeMillis());

                    LOGGER.info("Writing items into repository ...");
                    repository.store(items);
                } catch (IOException e) {
                    LOGGER.error("Could not import item ids {}", chunk, e);
                }
            });
        }

        executorService.shutdown();
        executorService.awaitTermination(10, TimeUnit.MINUTES);

        long t1 = System.currentTimeMillis();
        LOGGER.info("Imported the trading post within {} ms.", t1 - t0);
    }
}
