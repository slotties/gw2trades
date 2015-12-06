package gw2trades.server.security

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import javax.servlet.FilterChain
import javax.servlet.http.HttpServlet
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class RemoteAddrFilterSpec extends Specification {
    @Subject
    RemoteAddrFilter remoteAddrFilter = new RemoteAddrFilter()

    def request = Mock(HttpServletRequest)
    def response = Mock(HttpServletResponse)
    def filterChain = Mock(FilterChain)

    def allowClientIp() {
        given:
        remoteAddrFilter.setAllow("127\\.0\\.0\\.1")
        request.getHeader("X-Forwarded-for") >> "127.0.0.1"

        when:
        remoteAddrFilter.doFilter(request, response, filterChain)

        then:
        1 * filterChain.doFilter(request, response)
        0 * response.sendError(_)
    }

    def denyClientIp() {
        given:
        remoteAddrFilter.setAllow("127\\.0\\.0\\.1")
        request.getHeader("X-Forwarded-for") >> "192.168.1.1"

        when:
        remoteAddrFilter.doFilter(request, response, filterChain)

        then:
        0 * filterChain.doFilter(request, response)
        1 * response.sendError(403)
    }

    def fallbackToRequestRemoteAddr() {
        given:
        remoteAddrFilter.setAllow("127\\.0\\.0\\.1")
        request.getHeader("X-Forwarded-for") >> null
        request.getRemoteAddr() >> "127.0.0.1"

        when:
        remoteAddrFilter.doFilter(request, response, filterChain)

        then:
        1 * filterChain.doFilter(request, response)
        0 * response.sendError(_)
    }
}
