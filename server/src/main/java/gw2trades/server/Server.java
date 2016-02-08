package gw2trades.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.StaticHandler;

import java.io.File;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Server extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();

        Router router = Router.router(vertx);
        router.routeWithRegex("/static/.*").handler(StaticHandler.create(new File(".").getAbsolutePath()));

        // TODO: read port from config
        server.requestHandler(router::accept).listen(8080);
    }
}
