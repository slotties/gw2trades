package gw2trades.server.security;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class RemoteAddrFilter { /*extends RequestFilter {
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
    */
}
