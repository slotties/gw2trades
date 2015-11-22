package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
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
import java.util.*;
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

        List<ItemListingStatistics> itemListingStats = allStats.stream()
                .map(stats -> new ItemListingStatistics(itemOrNull(stats.getItemId()), stats))
                .collect(Collectors.toList());

        model.addObject("seoMeta", new SeoMeta("List of all items"));
        model.addObject("view", "index");
        model.addObject("lastPage", lastPage);
        model.addObject("currentPage", page);
        model.addObject("listingStatistics", itemListingStats);
        model.addObject("orderBy", orderBy);
        model.addObject("orderDir", orderDir);

        return model;
    }

    private Item itemOrNull(int itemId) {
        try {
            return itemRepository.getItem(itemId);
        } catch (IOException e) {
            Item item = new Item();
            item.setItemId(itemId);
            item.setName("BAD_ITEM");
            item.setIconUrl("javascript:void(0);");
            return item;
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
