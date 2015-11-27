package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.Order;
import gw2trades.repository.api.Query;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.server.frontend.sorters.*;
import gw2trades.server.model.ItemListingStatistics;
import gw2trades.server.model.SeoMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class IndexController {
    @Autowired
    private ItemRepository itemRepository;

    private int pageSize = 10;

    @RequestMapping("/index.html")
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
        List<ListingStatistics> allStats = new ArrayList<>(getStatistics(query, order));

        int lastPage = (int) Math.ceil((float) allStats.size() / (float) this.pageSize);

        int from = Math.max(0, Math.abs(pageSize * (page - 1)));
        int to = Math.min(allStats.size(), from + pageSize);
        allStats = allStats.subList(from, to);

        model.addObject("seoMeta", new SeoMeta("List of all items"));
        model.addObject("view", "index");
        model.addObject("lastPage", lastPage);
        model.addObject("currentPage", page);
        model.addObject("listingStatistics", allStats);
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

    private Collection<ListingStatistics> getStatistics(Query query, Order order) throws IOException {
        if (query != null) {
            return this.itemRepository.queryStatistics(query);
        } else {
            return this.itemRepository.listStatistics(order, 0, pageSize);
        }
    }

    private Comparator<ListingStatistics> resolveComparator(String orderBy, String orderDir) {
        Comparator<ListingStatistics> cmp = null;

        switch (orderBy) {
            case "highestBidder":
                cmp = new BuyersByMaxPrice();
                break;
            case "avgBidder":
                cmp = new BuyersByAveragePrice();
                break;
            case "lowestSeller":
                cmp = new SellersByMinPrice();
                break;
            case "avgSeller":
                cmp = new SellersByAveragePrice();
                break;
        }

        if (cmp != null) {
            if ("desc".equals(orderDir)) {
                cmp = new ReverseComparator<>(cmp);
            }
        }

        return cmp;
    }
}
