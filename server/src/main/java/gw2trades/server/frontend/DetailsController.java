package gw2trades.server.frontend;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class DetailsController {
    @Autowired
    private ItemRepository itemRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @RequestMapping("/details/{itemId}.html")
    public ModelAndView details(@PathVariable int itemId) throws IOException {
        ModelAndView model = new ModelAndView("frame");

        // TODO: check if dates are correctly, probably use system timezone instead of UTC?
        LocalDateTime to = LocalDateTime.now();
        LocalDateTime from = to.minus(2, ChronoUnit.WEEKS);

        List<ListingStatistics> history = itemRepository.getHistory(itemId, from.toInstant(ZoneOffset.UTC).toEpochMilli(), to.toInstant(ZoneOffset.UTC).toEpochMilli());
        ListingStatistics latestStats = itemRepository.latestStatistics(itemId);

        model.addObject("latestStats", latestStats);
        model.addObject("history", this.objectMapper.writeValueAsString(history));
        model.addObject("view", "details");

        return model;
    }
}