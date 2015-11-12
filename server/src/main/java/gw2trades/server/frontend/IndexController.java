package gw2trades.server.frontend;

import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.PriceStatistics;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class IndexController {
    @RequestMapping("/index.html")
    public ModelMap index() {
        ModelMap model = new ModelMap();
        // TODO: bind against item repo
        List<ListingStatistics> allStats = new ArrayList<>();
        ListingStatistics stats = new ListingStatistics();
        stats.setItemId(123);
        PriceStatistics buyStats = new PriceStatistics();
        buyStats.setMinPrice(1);
        buyStats.setMaxPrice(5);
        buyStats.setAverage(3.5);
        stats.setBuyStatistics(buyStats);
        PriceStatistics sellStats = new PriceStatistics();
        sellStats.setMinPrice(10);
        sellStats.setMaxPrice(50);
        sellStats.setAverage(30.5);
        stats.setSellStatistics(sellStats);

        allStats.add(stats);

        model.addAttribute("listingStatistics", allStats);

        return model;
    }
}
