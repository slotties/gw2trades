package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class AdminController {
    private ItemRepository itemRepository;

    @Autowired
    public AdminController(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @RequestMapping("/admin/reopenRepository")
    public @ResponseBody String reopenRepository() throws IOException {
        this.itemRepository.reopen();
        return "OK";
    }

    @RequestMapping("/admin/sitemap.xml")
    public ModelAndView createSitemapXml() throws IOException {
        ModelAndView mav = new ModelAndView("sitemapXml");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String now = sdf.format(new Date());

        mav.addObject("stats", itemRepository.listStatistics(null, null, 0, 5000));
        mav.addObject("now", now);

        return mav;
    }
}
