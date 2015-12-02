package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.SearchResult;
import gw2trades.server.model.SeoMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Locale;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class IndexController {
    private ItemRepository itemRepository;

    private int pageSize = 10;

    @Autowired
    public IndexController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @RequestMapping("/")
    public RedirectView root(HttpServletRequest request) {
        Locale locale = request.getLocale();
        if (isUnknownLocale(locale)) {
            locale = Locale.ENGLISH;
        }

        RedirectView redirectView = new RedirectView("/" + locale.getLanguage() + "/index.html");
        redirectView.setStatusCode(HttpStatus.MOVED_PERMANENTLY);

        return redirectView;
    }

    private boolean isUnknownLocale(Locale locale) {
        return !(Locale.GERMAN.equals(locale) || Locale.ENGLISH.equals(locale));
    }

    @RequestMapping("**/index.html")
    public ModelAndView index(
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String name) throws IOException {

        if (page < 1) {
            return new ModelAndView(new RedirectView("/index.html?page=1"));
        }

        ModelAndView model = new ModelAndView("frame");

        Query query = createQuery(name);
        Order order = orderBy != null ?  Order.by(orderBy, !"asc".equals(orderDir)) : null;
        SearchResult<ListingStatistics> results = getStatistics(query, order, page);

        int lastPage = (int) Math.ceil((float) results.getTotalResults() / (float) this.pageSize);

        model.addObject("seoMeta", new SeoMeta("index.title.default"));
        model.addObject("view", "index");
        model.addObject("lastPage", lastPage);
        model.addObject("currentPage", page);
        model.addObject("listingStatistics", results.getResults());
        model.addObject("orderBy", orderBy);
        model.addObject("orderDir", orderDir);
        model.addObject("query", query);

        return model;
    }

    private Query createQuery(String name) {
        Query query = null;
        if (name != null) {
            query = new Query();
            query.setName(name);
        }

        return query;
    }

    private SearchResult<ListingStatistics> getStatistics(Query query, Order order, int forPage) throws IOException {
        // forPage is 1-based, for easier readability in the URL.
        int fromPage = (forPage - 1) * this.pageSize;
        int toPage = fromPage + this.pageSize;

        return this.itemRepository.listStatistics(query, order, fromPage, toPage);
    }
}
