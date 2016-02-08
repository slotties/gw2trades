package gw2trades.server.frontend.exception;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.ErrorHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ExceptionHandler implements Handler<RoutingContext> {
    private static final Logger LOGGER = LogManager.getLogger(ExceptionHandler.class);

    private final Handler<RoutingContext> error404;
    private final Handler<RoutingContext> error500;

    public ExceptionHandler() {
        this.error404 = ErrorHandler.create("static/404.html", false);
        this.error500 = ErrorHandler.create("static/500.html", false);
    }

    @Override
    public void handle(RoutingContext event) {
        Throwable exception = event.failure();

        if (exception instanceof ItemNotFoundException) {
            LOGGER.warn("An item was not found ({})", event.request().path(), exception);
            // FIXME: set status code to 404, seems to be harder than expected :(
            error404.handle(event);
        } else {
            LOGGER.error("Failed to load URL {}", event.request().path(), exception);
            error500.handle(event);
        }
    }
}
