package gw2trades.server;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerResponse;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Server extends AbstractVerticle {
    @Override
    public void start() throws Exception {
        HttpServer server = vertx.createHttpServer();
        server.requestHandler(request -> {
            HttpServerResponse response = request.response();
            response.end("blub");
        });

        // TODO: read port from config
        server.listen(8080);
    }
}
