package gw2trades.server.frontend

import gw2trades.repository.api.ItemRepository
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDateTime

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class HistoryHandlerSpec extends Specification {
    @Subject
    HistoryHandler historyHandler
    ItemRepository itemRepository

    def setup() {
        itemRepository = Mock(ItemRepository)
        historyHandler = new HistoryHandler(itemRepository)
    }

    def list() {
        given:
        def fromDateStr = "1984-06-24T10:00:00"
        def expFromDate = LocalDateTime.parse(fromDateStr, HistoryHandler.DATE_FORMAT)
        def toDateStr = "2002-06-24T10:00:00"
        def expToDate = LocalDateTime.parse(toDateStr, HistoryHandler.DATE_FORMAT)

        when:
        def queryParams = Mock(MultiMap)
        queryParams.get("from") >> fromDateStr
        queryParams.get("to") >> toDateStr

        def request = Mock(HttpServerRequest)
        request.path() >> "/api/history/123"
        request.params() >> queryParams

        def routingContext = Mock(RoutingContext)
        routingContext.request() >> request
        routingContext.response() >> Mock(HttpServerResponse)

        historyHandler.handle(routingContext)

        then:
        1 * itemRepository.getHistory(123, expFromDate, expToDate)
    }

    @Unroll("from=#from and to=#to")
    def listWithBadValues(String from, String to) {
        given:
        def queryParams = Mock(MultiMap)
        queryParams.get("from") >> from
        queryParams.get("to") >> to

        def request = Mock(HttpServerRequest)
        request.path() >> "/api/history/123"
        request.params() >> queryParams

        def routingContext = Mock(RoutingContext)
        routingContext.request() >> request
        routingContext.response() >> Mock(HttpServerResponse)

        when:
        historyHandler.handle(routingContext)

        then:
        1 * routingContext.fail(*_) >> { args ->
            assert args[0] instanceof IllegalArgumentException
        }

        where:
        from                  | to
        null                  | "1984-06-24T10:00:00"
        "1984-06-24T10:00:00" | null
        "1984-06-24T10:00"    | "1984-06-24T10:00:00"
        "1984-06-24 10:00:00" | "1984-06-24T10:00:00"
    }
}
