package gw2trades.server;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.influxdb.InfluxDbConnectionManagerImpl;
import gw2trades.repository.influxdb.InfluxDbRepository;
import gw2trades.repository.lucene.LuceneRecipeRepository;
import gw2trades.server.frontend.ReopenRepositoryHandler;
import gw2trades.server.frontend.SitemapHandler;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Server extends AbstractVerticle {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfig.class);

    private ItemRepository itemRepository;
    private RecipeRepository recipeRepository;
    private VelocityEngine velocityEngine;

    @Override
    public void start() throws Exception {
        initVelocity();
        openRepositories();
        startWebServer();
    }

    private void initVelocity() {
        Properties props = new Properties();
        props.setProperty("file.resource.loader.path", config().getString("templates.dir"));
        props.setProperty("file.resource.loader.cache", Boolean.toString(!config().getBoolean("resources.disableCaching", false)));
        props.setProperty("resource.loader", "file");

        velocityEngine = new VelocityEngine(props);
    }

    private void openRepositories() throws IOException {
        InfluxDbConnectionManagerImpl connectionManager = new InfluxDbConnectionManagerImpl(
                config().getString("influx.url"),
                config().getString("influx.user"),
                config().getString("influx.pass")
        );

        String indexDir = config().getString("index.dir.items");
        LOGGER.info("Using {} as item index directory.", indexDir);

        this.itemRepository = new InfluxDbRepository(connectionManager, indexDir, true);

        indexDir = config().getString("index.dir.recipes");
        LOGGER.info("Using {} as recipe index directory.", indexDir);

        this.recipeRepository = new LuceneRecipeRepository(indexDir, true);
    }

    private void startWebServer() {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);

        router.routeWithRegex("/static/.*").handler(
                StaticHandler
                        .create(new File(".").getAbsolutePath())
                        .setCachingEnabled(config().getBoolean("resources.disableCaching", false))
        );

        router.route("/admin/reopenRepository").handler(new ReopenRepositoryHandler(itemRepository, recipeRepository));
        router.route("/admin/sitemap.xml").handler(new SitemapHandler(itemRepository, velocityEngine));

        server.requestHandler(router::accept).listen(config().getInteger("http.port"));
    }
}
