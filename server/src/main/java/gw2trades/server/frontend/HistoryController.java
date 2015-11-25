package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ListingStatistics;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Controller
public class HistoryController {
    static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    @Autowired
    private ItemRepository repository;

    @RequestMapping(value = "/api/history/{itemId}", method = RequestMethod.GET)
    public @ResponseBody List<ListingStatistics> list(
            @PathVariable int itemId,
            @RequestParam String from,
            @RequestParam String to
    ) throws ParseException, IOException {
        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        long fromTs = format.parse(from).getTime();
        long toTs = format.parse(to).getTime();

        return repository.getHistory(itemId, fromTs, toTs);
    }
}
