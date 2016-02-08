package gw2trades.server.locale

import spock.lang.Specification

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
class LocaleInterceptorSpec extends Specification {
    /*
    @Subject
    LocaleInterceptor localeInterceptor = new LocaleInterceptor()
    HttpServletRequest request = Mock(HttpServletRequest)
    LocaleResolver localeResolver = Mock(LocaleResolver)

    @Unroll("test with url=#url")
    def testUrls(String url, Locale expectedLocale) {
        given:
        request.getRequestURI() >> url
        request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE) >> localeResolver

        when:
        localeInterceptor.preHandle(request, null, null)

        then:
        1 * localeResolver.setLocale(request, _, expectedLocale)

        where:
        url               | expectedLocale
        "/de/index.html"  | Locale.GERMAN
        "/en/index.html"  | Locale.ENGLISH
    }

    @Unroll("bad urls with url=#url")
    def testBadUrls(String url) {
        given:
        request.getRequestURI() >> url
        request.getAttribute(DispatcherServlet.LOCALE_RESOLVER_ATTRIBUTE) >> localeResolver

        when:
        localeInterceptor.preHandle(request, null, null)

        then:
        // The locale resolver must have not been called because the URLs do not contain any recognized locale.
        0 * localeResolver.setLocale(request, _, _)

        where:
        url << [
                "/der/index.html",
                "/index.html",
                "/"
        ]
    }
    */
}
