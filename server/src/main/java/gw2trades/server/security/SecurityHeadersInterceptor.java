package gw2trades.server.security;

import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SecurityHeadersInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        response.addHeader("X-Frame-Options", "SAMEORIGIN");
        response.addHeader("X-XSS-Protection", "1; mode=block");

        return true;
    }
}
