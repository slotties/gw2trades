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

    private ItemRepository repository;

    @Autowired
    public HistoryController(ItemRepository itemRepository) {
        this.repository = itemRepository;
    }

    @RequestMapping(value = "/api/history/{itemId}", method = RequestMethod.GET)
    public @ResponseBody List<ListingStatistics> list(
            @PathVariable int itemId,
            @RequestParam String from,
            @RequestParam String to
    ) throws IllegalArgumentException, IOException {
        if (from == null || to == null) {
            throw new IllegalArgumentException("bad input dates");
        }

        SimpleDateFormat format = new SimpleDateFormat(DATE_FORMAT);
        long fromTs;
        long toTs;
        try {
            fromTs = format.parse(from).getTime();
            toTs = format.parse(to).getTime();
        } catch (ParseException e) {
            throw new IllegalArgumentException("bad input dates");
        }

        return repository.getHistory(itemId, fromTs, toTs);
    }
}
