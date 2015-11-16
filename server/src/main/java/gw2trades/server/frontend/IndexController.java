package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.server.frontend.sorters.BuyersByAveragePrice;
import gw2trades.server.frontend.sorters.BuyersByMaxPrice;
import gw2trades.server.frontend.sorters.ReverseComparator;
import gw2trades.server.frontend.sorters.SellersByAveragePrice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

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
            @RequestParam(defaultValue = "1") int page) throws IOException {

        if (page < 1) {
            return new ModelAndView(new RedirectView("/index.html?page=1"));
        }

        ModelAndView model = new ModelAndView("frame");

        List<ListingStatistics> allStats = new ArrayList<>(itemRepository.listStatistics());
        if (StringUtils.hasText(orderBy) && StringUtils.hasText(orderDir)) {
            Comparator<ListingStatistics> cmp = resolveComparator(orderBy, orderDir);
            if (cmp != null) {
                allStats.sort(cmp);
            }
        }

        int lastPage = (int) Math.ceil((float) allStats.size() / (float) this.pageSize);

        int from = Math.max(0, Math.abs(pageSize * (page - 1)));
        int to = Math.min(allStats.size(), from + pageSize);
        allStats = allStats.subList(from, to);

        model.addObject("view", "index");
        model.addObject("lastPage", lastPage);
        model.addObject("currentPage", page);
        model.addObject("listingStatistics", allStats);
        model.addObject("orderBy", orderBy);
        model.addObject("orderDir", orderDir);

        return model;
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
                cmp = new BuyersByMaxPrice();
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
