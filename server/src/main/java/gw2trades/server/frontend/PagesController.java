package gw2trades.server.frontend;

import gw2trades.server.model.SeoMeta;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class PagesController {
    @RequestMapping("/impressum.html")
    public ModelAndView impressum() {
        ModelAndView mav = new ModelAndView("frame");
        SeoMeta meta = new SeoMeta("Impressum");

        mav.addObject("view", "impressum");
        mav.addObject("seoMeta", meta);

        return mav;
    }
}
