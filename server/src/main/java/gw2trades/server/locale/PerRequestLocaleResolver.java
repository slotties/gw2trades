package gw2trades.server.locale;

import org.springframework.web.servlet.i18n.AbstractLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Keep the current locale as request attribute instead of having either a server-side persistent state such as a session
 * or a client-side persistent state such as a cookie. The locale is in the URL on every call anyway.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class PerRequestLocaleResolver extends AbstractLocaleResolver {
    private static final String REQ_ATTR = "PerRequestLocaleResolver.locale";

    public PerRequestLocaleResolver() {
        super();
        setDefaultLocale(Locale.ENGLISH);
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        Locale locale = (Locale) request.getAttribute(REQ_ATTR);
        if (locale == null) {
            locale = getDefaultLocale();
        }

        return locale;
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        request.setAttribute(REQ_ATTR, locale);
    }
}
