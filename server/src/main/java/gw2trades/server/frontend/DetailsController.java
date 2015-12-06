package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.server.model.GoogleAnalytics;
import gw2trades.server.model.SeoMeta;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class DetailsController {
    private ItemRepository itemRepository;

    @Autowired
    public DetailsController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @RequestMapping("**/details/{itemId}.html")
    public ModelAndView details(@PathVariable int itemId) throws IOException {
        ModelAndView model = new ModelAndView("frame");
        ListingStatistics latestStats = itemRepository.latestStatistics(itemId);

        SeoMeta seoMeta = new SeoMeta("details.title");
        seoMeta.setTitleArgs(new Object[] { latestStats.getItem().getName() });
        seoMeta.setImageUrl(latestStats.getItem().getIconUrl());
        seoMeta.setDescription("details.description");
        seoMeta.setDescriptionArgs(new Object[] { latestStats.getItem().getName() });
        seoMeta.setKeywords(latestStats.getItem().getName());

        GoogleAnalytics googleAnalytics = new GoogleAnalytics();
        googleAnalytics.getDimensions().add(Integer.toString(latestStats.getItem().getItemId()));

        model.addObject("googleAnalytics", googleAnalytics);
        model.addObject("seoMeta", seoMeta);
        model.addObject("latestStats", latestStats);
        model.addObject("view", "details");

        return model;
    }
}
