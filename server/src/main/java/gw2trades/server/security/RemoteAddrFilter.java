package gw2trades.server.security;

import org.apache.catalina.comet.CometEvent;
import org.apache.catalina.comet.CometFilterChain;
import org.apache.catalina.filters.RequestFilter;
import org.apache.juli.logging.Log;
import org.apache.juli.logging.LogFactory;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class RemoteAddrFilter extends RequestFilter {
    private static final Log log = LogFactory.getLog(RemoteAddrFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        process(resolveClientIp((HttpServletRequest) request), request, response, chain);
    }

    @Override
    public void doFilterEvent(CometEvent event, CometFilterChain chain) throws IOException, ServletException {
        processCometEvent(resolveClientIp(event.getHttpServletRequest()), event, chain);
    }

    @Override
    protected Log getLogger() {
        return log;
    }

    private String resolveClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-for");
        if (clientIp == null) {
            clientIp = request.getRemoteAddr();
        }

        return clientIp;
    }
}
