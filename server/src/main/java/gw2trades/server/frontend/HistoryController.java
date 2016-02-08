package gw2trades.server.frontend;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class HistoryController {
    /*
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

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

        LocalDateTime fromTs;
        LocalDateTime toTs;
        try {
            fromTs = LocalDateTime.parse(from, DATE_FORMAT);
            toTs = LocalDateTime.parse(to, DATE_FORMAT);
        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("bad input dates");
        }

        return repository.getHistory(itemId, fromTs, toTs);
    }
    */
}
