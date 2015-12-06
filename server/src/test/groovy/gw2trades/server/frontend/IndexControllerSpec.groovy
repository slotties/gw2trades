package gw2trades.server.frontend

import gw2trades.repository.api.ItemRepository
import gw2trades.repository.api.Order
import gw2trades.repository.api.Query
import gw2trades.repository.api.model.ListingStatistics
import gw2trades.repository.api.model.SearchResult
import org.springframework.web.servlet.view.RedirectView
import spock.lang.Specification
import spock.lang.Subject

import javax.servlet.http.HttpServletRequest

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class IndexControllerSpec extends Specification {
    @Subject
    IndexController indexController
    ItemRepository itemRepository

    def setup() {
        itemRepository = Mock(ItemRepository)
        indexController = new IndexController(itemRepository)
    }

    def root() {
        given:
        def request = Mock(HttpServletRequest)
        request.getLocale() >> Locale.GERMAN

        when:
        def view = indexController.root(request)

        then:
        view instanceof RedirectView
        ((RedirectView) view).getUrl() == "/de/index.html"
    }

    def firstPageIndex() {
        given:
        def request = Mock(HttpServletRequest)
        request.getLocale() >> Locale.GERMAN
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        itemRepository.listStatistics(null, null, 0, IndexController.PAGE_SIZE) >> searchResults

        when:
        def modelAndView = indexController.index(null, null, 1, null)

        then:
        modelAndView.model.listingStatistics == []
        modelAndView.model.currentPage == 1
        modelAndView.model.lastPage == 4
        modelAndView.model.orderBy == null
        modelAndView.model.orderDir == null
        modelAndView.model.query == null
    }

    def somePageIndex() {
        given:
        def request = Mock(HttpServletRequest)
        request.getLocale() >> Locale.GERMAN
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        itemRepository.listStatistics(null, null, IndexController.PAGE_SIZE, 2 * IndexController.PAGE_SIZE) >> searchResults

        when:
        def modelAndView = indexController.index(null, null, 2, null)

        then:
        modelAndView.model.listingStatistics == []
        modelAndView.model.currentPage == 2
        modelAndView.model.lastPage == 4
        modelAndView.model.orderBy == null
        modelAndView.model.orderDir == null
        modelAndView.model.query == null
    }

    def query() {
        given:
        def request = Mock(HttpServletRequest)
        request.getLocale() >> Locale.GERMAN
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        Query query = new Query(
                name: "foo"
        )
        itemRepository.listStatistics(query, null, 0, IndexController.PAGE_SIZE) >> searchResults

        when:
        def modelAndView = indexController.index(null, null, 1, "foo")

        then:
        modelAndView.model.listingStatistics == []
        modelAndView.model.currentPage == 1
        modelAndView.model.lastPage == 4
        modelAndView.model.orderBy == null
        modelAndView.model.orderDir == null
        modelAndView.model.query == query
    }

    def sort() {
        given:
        def request = Mock(HttpServletRequest)
        request.getLocale() >> Locale.GERMAN
        def searchResults = new SearchResult<ListingStatistics>(
                [], 101
        )
        Order order = Order.by("foo", true)
        itemRepository.listStatistics(null, order, 0, IndexController.PAGE_SIZE) >> searchResults

        when:
        def modelAndView = indexController.index("foo", "desc", 1, null)

        then:
        modelAndView.model.listingStatistics == []
        modelAndView.model.currentPage == 1
        modelAndView.model.lastPage == 4
        modelAndView.model.orderBy == "foo"
        modelAndView.model.orderDir == "desc"
        modelAndView.model.query == null
    }
}
