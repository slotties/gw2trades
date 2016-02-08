package gw2trades.server.frontend

import gw2trades.repository.api.ItemRepository
import gw2trades.repository.api.Order
import gw2trades.repository.api.Query
import gw2trades.repository.api.model.ListingStatistics
import gw2trades.repository.api.model.SearchResult
import gw2trades.server.VelocityRenderer
import io.vertx.core.MultiMap
import io.vertx.core.http.HttpServerRequest
import io.vertx.core.http.HttpServerResponse
import io.vertx.ext.web.RoutingContext
import spock.lang.Specification
import spock.lang.Subject


/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class IndexHandlerSpec extends Specification {
    @Subject
    IndexHandler indexHandler
    ItemRepository itemRepository
    VelocityRenderer renderer
    RoutingContext routingContext
    HttpServerRequest request
    Map contextData
    MultiMap requestParams
    HttpServerResponse response

    def setup() {
        itemRepository = Mock(ItemRepository)
        renderer = Mock(VelocityRenderer)
        indexHandler = new IndexHandler(itemRepository, renderer)

        request = Mock(HttpServerRequest)
        response = Mock(HttpServerResponse)
        contextData = [:]

        requestParams = Mock(MultiMap)
        request.params() >> requestParams

        routingContext = Mock(RoutingContext)
        routingContext.request() >> request
        routingContext.data() >> contextData
        routingContext.response() >> response
    }

    def firstPageIndex() {
        given:
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        itemRepository.listStatistics(null, null, 0, IndexHandler.PAGE_SIZE) >> searchResults

        when:
        requestParams.get("page") >> "1"
        indexHandler.handle(routingContext)

        then:
        1 * renderer.render(*_) >> { args ->
            Map ctx = args[2]
            assert ctx.listingStatistics == []
            assert ctx.currentPage == 1
            assert ctx.lastPage == 4
            assert ctx.orderBy == null
            assert ctx.orderDir == null
            assert ctx.query == null
        }
    }

    def somePageIndex() {
        given:
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        itemRepository.listStatistics(null, null, IndexHandler.PAGE_SIZE, 2 * IndexHandler.PAGE_SIZE) >> searchResults

        when:
        requestParams.get("page") >> "2"
        indexHandler.handle(routingContext)

        then:
        1 * renderer.render(*_) >> { args ->
            Map ctx = args[2]
            assert ctx.listingStatistics == []
            assert ctx.currentPage == 2
            assert ctx.lastPage == 4
            assert ctx.orderBy == null
            assert ctx.orderDir == null
            assert ctx.query == null
        }
    }

    def query() {
        given:
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        Query query = new Query(
                name: "foo"
        )
        itemRepository.listStatistics(query, null, 0, IndexHandler.PAGE_SIZE) >> searchResults

        when:
        requestParams.get("page") >> "1"
        requestParams.get("name") >> "foo"
        indexHandler.handle(routingContext)

        then:
        1 * renderer.render(*_) >> { args ->
            Map ctx = args[2]
            assert ctx.listingStatistics == []
            assert ctx.currentPage == 1
            assert ctx.lastPage == 4
            assert ctx.orderBy == null
            assert ctx.orderDir == null
            assert ctx.query == query
        }
    }

    def sort() {
        given:
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        Order order = Order.by("foo", true)
        itemRepository.listStatistics(null, order, 0, IndexHandler.PAGE_SIZE) >> searchResults

        when:
        requestParams.get("page") >> "1"
        requestParams.get("orderBy") >> "foo"
        requestParams.get("orderDir") >> "desc"
        indexHandler.handle(routingContext)

        then:
        1 * renderer.render(*_) >> { args ->
            Map ctx = args[2]
            assert ctx.listingStatistics == []
            assert ctx.currentPage == 1
            assert ctx.lastPage == 4
            assert ctx.orderBy == "foo"
            assert ctx.orderDir == "desc"
            assert ctx.query == null
        }
    }
}
