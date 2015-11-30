package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.io.IOException;

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
}
