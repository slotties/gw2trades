package gw2trades.server;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.influxdb.InfluxDbConnectionManagerImpl;
import gw2trades.repository.influxdb.InfluxDbRepository;
import gw2trades.repository.lucene.LuceneRecipeRepository;
import gw2trades.server.frontend.ImprintHandler;
import gw2trades.server.frontend.RedirectIndexHandler;
import gw2trades.server.frontend.ReopenRepositoryHandler;
import gw2trades.server.frontend.SitemapHandler;
import gw2trades.server.i18n.LocaleHandler;
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
    private VelocityRenderer renderer;

    @Override
    public void start() throws Exception {
        initRenderer();
        openRepositories();
        startWebServer();
    }

    private void initRenderer() {
        Properties props = new Properties();
        props.setProperty("file.resource.loader.path", config().getString("templates.dir"));
        props.setProperty("file.resource.loader.cache", Boolean.toString(!config().getBoolean("resources.disableCaching", false)));
        props.setProperty("resource.loader", "file");

        renderer = new VelocityRenderer(new VelocityEngine(props));
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
        LocaleHandler localeHandler = new LocaleHandler();

        router.routeWithRegex("/static/.*").handler(
                StaticHandler
                        .create(new File(".").getAbsolutePath())
                        .setCachingEnabled(config().getBoolean("resources.disableCaching", false))
        );

        router.route("/admin/reopenRepository").handler(new ReopenRepositoryHandler(itemRepository, recipeRepository));
        router.route("/admin/sitemap.xml").handler(new SitemapHandler(itemRepository, renderer));

        router.routeWithRegex("/.*/impressum.html").handler(localeHandler);
        router.routeWithRegex("/.*/impressum.html").handler(new ImprintHandler(renderer));

        router.routeWithRegex("/").handler(localeHandler);
        router.routeWithRegex("/").handler(new RedirectIndexHandler());

        server.requestHandler(router::accept).listen(config().getInteger("http.port"));
    }
}
