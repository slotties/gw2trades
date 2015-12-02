package gw2trades.server.locale;

import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class LocaleInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        if (path.endsWith(".html")) {
            String localeStr = resolveLocaleString(path);
            if (localeStr != null) {
                LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
                localeResolver.setLocale(request, response, StringUtils.parseLocaleString(localeStr));
            }
        }

        return true;
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
}
