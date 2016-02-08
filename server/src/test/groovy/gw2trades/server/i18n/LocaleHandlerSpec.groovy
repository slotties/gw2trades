package gw2trades.server.i18n

import io.vertx.core.http.HttpServerRequest
import io.vertx.ext.web.RoutingContext
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
class LocaleHandlerSpec extends Specification {
    @Subject
    LocaleHandler localeHandler = new LocaleHandler()

    @Unroll("test with url=#url")
    def testUrls(String url, Locale expectedLocale) {
        given:
        def request = Mock(HttpServerRequest)
        request.uri() >> url

        def routingContext = Mock(RoutingContext)
        routingContext.request() >> request

        def data = [:]
        routingContext.data() >> data

        when:
        localeHandler.handle(routingContext)

        then:
        data.size() == 1
        data.currentLocale == expectedLocale

        where:
        url               | expectedLocale
        "/de/index.html"  | Locale.GERMAN
        "/en/index.html"  | Locale.ENGLISH
        "/der/index.html" | Locale.ENGLISH
        "/index.html"     | Locale.ENGLISH
        "/"               | Locale.ENGLISH
    }
}
