package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class IndexController {
    @Autowired
    private ItemRepository itemRepository;

    @RequestMapping("/index.html")
    public ModelMap index() throws IOException {
        ModelMap model = new ModelMap();

        List<ListingStatistics> allStats = new ArrayList<>(itemRepository.listStatistics());
        // TODO: sort by
        // TODO: paging

        model.addAttribute("listingStatistics", allStats);

        return model;
    }
}
