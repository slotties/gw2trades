package gw2trades.server.security;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SecurityHeadersHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext event) {
        event.response().putHeader("X-Frame-Options", "SAMEORIGIN");
        event.response().putHeader("X-XSS-Protection", "1; mode=block");
        event.next();
    }
}
