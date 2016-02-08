package gw2trades.server.i18n;

import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.apache.commons.lang.LocaleUtils;

import java.util.Locale;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class LocaleHandler implements Handler<RoutingContext> {
    private static final String CTX_CURRENT_LOCALE = "currentLocale";

    @Override
    public void handle(RoutingContext event) {
        Locale currentLocale = null;

        String path = event.request().uri();
        if (path.endsWith(".html")) {
            String localeStr = resolveLocaleString(path);
            if (localeStr != null) {
                currentLocale = LocaleUtils.toLocale(localeStr);
            }
        }

        if (currentLocale == null) {
            currentLocale = Locale.ENGLISH;
        }

        event.data().put(CTX_CURRENT_LOCALE, currentLocale);
        event.next();
    }

    private String resolveLocaleString(String path) {
        // The schema is always: /[locale]/* where [locale] is always a 2-letter language code. Therefore we have to expect
        // an end index of 3 and nothing else.
        int endIdx = path.indexOf('/', 1);
        if (endIdx != 3) {
            return null;
        }

        return path.substring(1, endIdx);
    }

    public static Locale getLocale(RoutingContext ctx) {
        Locale locale = (Locale) ctx.data().get(CTX_CURRENT_LOCALE);
        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        return locale;
    }
}
