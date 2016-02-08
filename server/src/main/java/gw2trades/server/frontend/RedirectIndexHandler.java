package gw2trades.server.frontend;

import gw2trades.server.i18n.LocaleHandler;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.ext.web.RoutingContext;

import java.util.Locale;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class RedirectIndexHandler implements Handler<RoutingContext> {
    @Override
    public void handle(RoutingContext event) {
        Locale targetLocale = LocaleHandler.getLocale(event);

        HttpServerResponse response = event.response();
        response.setStatusCode(301);
        response.putHeader("Location", "/" + targetLocale.getLanguage() + "/index.html");
        response.end();
    }
}
