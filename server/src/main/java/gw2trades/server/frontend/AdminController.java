package gw2trades.server.frontend;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class AdminController {
    /*
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private ItemRepository itemRepository;
    private RecipeRepository recipeRepository;

    @Autowired
    public AdminController(ItemRepository itemRepository, RecipeRepository recipeRepository) {
        this.itemRepository = itemRepository;
        this.recipeRepository = recipeRepository;
    }

    @RequestMapping("/admin/reopenRepository")
    public @ResponseBody String reopenRepository() throws IOException {
        this.itemRepository.reopen();
        this.recipeRepository.reopen();

        return "OK";
    }

    @RequestMapping("/admin/sitemap.xml")
    public ModelAndView createSitemapXml() throws IOException {
        ModelAndView mav = new ModelAndView("sitemapXml");

        String now = DATE_FORMATTER.format(LocalDate.now());

        mav.addObject("stats", itemRepository.listStatistics(null, null, 0, Integer.MAX_VALUE));
        mav.addObject("now", now);

        return mav;
    }
    */
}
