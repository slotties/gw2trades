package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.api.model.Recipe;
import gw2trades.repository.influxdb.InfluxDbConnectionManager;
import gw2trades.repository.influxdb.InfluxDbConnectionManagerImpl;
import gw2trades.repository.influxdb.InfluxDbRepository;
import gw2trades.repository.lucene.LuceneRecipeRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.influxdb.InfluxDB;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

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
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        LOGGER.info("Importing with {} threads (each {} chunks) into {}...\n", threadCount, chunkSize, indexDir);

        LOGGER.info("Loading items ...");
        long t0 = System.currentTimeMillis();
        Map<Integer, Item> items = loadItems(chunkSize, executorService);
        long t1 = System.currentTimeMillis();
        LOGGER.info("Loaded  items in {} ms.", t1 - t0);

        LOGGER.info("Importing items ...");
        t0 = System.currentTimeMillis();
        importItems(items, chunkSize, connectionManager, indexDir + "/items", executorService);
        t1 = System.currentTimeMillis();
        LOGGER.info("Imported items in {} ms.", t1 - t0);

        LOGGER.info("Importing recipes ...");
        t0 = System.currentTimeMillis();
        importRecipes(items, chunkSize, indexDir + "/recipes", executorService);
        t1 = System.currentTimeMillis();
        LOGGER.info("Imported recipes in {} ms.", t1 - t0);

        executorService.shutdown();
    }

    private Map<Integer, Item> loadItems(int chunkSize, ExecutorService executorService) throws Exception {
        List<Callable<List<Item>>> tasks = new ArrayList<>();
        List<Integer> itemIds = tradingPost.listItemIds();
        for (int i = 0; i < itemIds.size(); i += chunkSize) {
            List<Integer> chunk = itemIds.subList(i, Math.min(itemIds.size(), i + chunkSize));
            tasks.add(new ItemPuller(tradingPost, chunk));
        }

        List<Future<List<Item>>> results = executorService.invokeAll(tasks);
        Map<Integer, Item> items = new HashMap<>(25000);
        for (Future<List<Item>> result : results) {
            for (Item item : result.get()) {
                items.put(item.getItemId(), item);
            }
        }

        if (items.isEmpty()) {
            LOGGER.error("The gw2 API did not return any items. It's broken again. Won't import anything.");
            System.exit(1);
        }

        return items;
    }

    private void importItems(Map<Integer, Item> items, int chunkSize, InfluxDbConnectionManager connectionManager, String indexDir, ExecutorService executorService) throws Exception {
        List<Callable<List<ItemListings>>> tasks = new ArrayList<>();
        List<Integer> itemIds = tradingPost.listItemIds();
        for (int i = 0; i < itemIds.size(); i += chunkSize) {
            List<Integer> chunk = itemIds.subList(i, Math.min(itemIds.size(), i + chunkSize));
            tasks.add(new ItemListingsPuller(tradingPost, chunk, items));
        }

        List<Future<List<ItemListings>>> results = executorService.invokeAll(tasks);
        List<ItemListings> listings = new ArrayList<>(items.size());
        for (Future<List<ItemListings>> result : results) {
            listings.addAll(result.get());
        }

        if (listings.isEmpty()) {
            LOGGER.error("The gw2 API did not return any listings. It's broken again. Won't import anything.");
            System.exit(1);
        }

        ItemRepository repository = new InfluxDbRepository(connectionManager, indexDir, false);
        try {
            LOGGER.info("Writing everything into repository ...");
            repository.store(listings, System.currentTimeMillis());
        } finally {
            repository.close();
        }
    }

    private void importRecipes(Map<Integer, Item> items, int chunkSize, String indexDir, ExecutorService executorService) throws Exception {
        List<Callable<List<Recipe>>> tasks = new ArrayList<>();
        List<Integer> ids = tradingPost.listRecipeIds();
        for (int i = 0; i < ids.size(); i += chunkSize) {
            List<Integer> chunk = ids.subList(i, Math.min(ids.size(), i + chunkSize));
            tasks.add(new RecipePuller(tradingPost, chunk, items));
        }

        List<Future<List<Recipe>>> results = executorService.invokeAll(tasks);
        List<Recipe> recipes = new ArrayList<>(items.size());
        for (Future<List<Recipe>> result : results) {
            recipes.addAll(result.get());
        }

        if (recipes.isEmpty()) {
            LOGGER.error("The gw2 API did not return any recipes. It's broken again. Won't import anything.");
            System.exit(1);
        }

        RecipeRepository repository = new LuceneRecipeRepository(indexDir, false);
        try {
            LOGGER.info("Writing everything into repository ...");
            repository.store(recipes);
        } finally {
            repository.close();
        }
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
