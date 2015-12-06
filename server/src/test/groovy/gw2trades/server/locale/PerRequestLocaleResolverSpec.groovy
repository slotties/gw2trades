package gw2trades.server.locale

import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class PerRequestLocaleResolverSpec extends Specification {
    @Subject
    PerRequestLocaleResolver localeResolver = new PerRequestLocaleResolver()
    HttpServletRequest request = Mock(HttpServletRequest)

    def setLocale() {
        when:
        localeResolver.setLocale(request, null, Locale.GERMAN)

        then:
        1 * request.setAttribute(PerRequestLocaleResolver.REQ_ATTR, Locale.GERMAN)
    }

    def resolveLocale() {
        given:
        request.getAttribute(PerRequestLocaleResolver.REQ_ATTR) >> Locale.GERMAN
        localeResolver.setDefaultLocale(Locale.JAPANESE)

        when:
        def locale = localeResolver.resolveLocale(request)

        then:
        locale == Locale.GERMAN
    }

    def resolveLocaleWithDefaultLocale() {
        given:
        request.getAttribute(PerRequestLocaleResolver.REQ_ATTR) >> null
        localeResolver.setDefaultLocale(Locale.JAPANESE)

        when:
        def locale = localeResolver.resolveLocale(request)

        then:
        locale == Locale.JAPANESE
    }
}
