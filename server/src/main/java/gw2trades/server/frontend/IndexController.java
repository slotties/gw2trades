package gw2trades.server.frontend;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class IndexController {
    /*
    private static final EscapeTool ESCAPE_TOOL = new EscapeTool();
    private static final int PAGE_SIZE = 30;

    private ItemRepository itemRepository;

*/
    //@RequestMapping("**/index.html")
    /*
    public ModelAndView index(
            @RequestParam(required = false) String orderBy,
            @RequestParam(required = false) String orderDir,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(required = false) String name) throws IOException {

        if (page < 1) {
            return new ModelAndView(new RedirectView("/"));
        }

        ModelAndView model = new ModelAndView("frame");

        Query query = createQuery(name);
        Order order = orderBy != null ? Order.by(orderBy, !"asc".equals(orderDir)) : null;
        SearchResult<ListingStatistics> results = getStatistics(query, order, page);

        int lastPage = (int) Math.ceil((float) results.getTotalResults() / (float) PAGE_SIZE);

        SeoMeta seoMeta = new SeoMeta("index.title.default");
        seoMeta.setDescription("index.description");
        model.addObject("seoMeta", seoMeta);
        model.addObject("view", "index");
        model.addObject("lastPage", lastPage);
        model.addObject("currentPage", page);
        model.addObject("listingStatistics", results.getResults());
        model.addObject("orderBy", ESCAPE_TOOL.html(orderBy));
        model.addObject("orderDir", ESCAPE_TOOL.html(orderDir));
        model.addObject("query", query);
        model.addObject("escapetool", ESCAPE_TOOL);

        return model;
    }

    private Query createQuery(String name) {
        Query query = null;
        if (name != null) {
            query = new Query();
            query.setName(name);
        }

        return query;
    }

    private SearchResult<ListingStatistics> getStatistics(Query query, Order order, int forPage) throws IOException {
        // forPage is 1-based, for easier readability in the URL.
        int fromPage = (forPage - 1) * PAGE_SIZE;
        int toPage = fromPage + PAGE_SIZE;

        return this.itemRepository.listStatistics(query, order, fromPage, toPage);
    }
    */
}
