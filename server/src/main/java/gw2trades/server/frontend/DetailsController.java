package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class DetailsController {
    private ItemRepository itemRepository;
    private RecipeRepository recipeRepository;

    /*
    @Autowired
    public DetailsController(ItemRepository itemRepository, RecipeRepository recipeRepository) {
        this.itemRepository = itemRepository;
        this.recipeRepository = recipeRepository;

    }

*/
//    @RequestMapping("**/details/{itemId}.html")
    /*
    public ModelAndView details(@PathVariable int itemId) throws IOException {
        ModelAndView model = new ModelAndView("frame");
        ListingStatistics latestStats = itemRepository.latestStatistics(itemId);
        if (latestStats == null) {
            throw new ItemNotFoundException();
        }

        SeoMeta seoMeta = createSeoMeta(latestStats);
        GoogleAnalytics googleAnalytics = new GoogleAnalytics();
        googleAnalytics.getDimensions().add(Integer.toString(latestStats.getItem().getItemId()));

        model.addObject("locale", LocaleContextHolder.getLocale());
        model.addObject("googleAnalytics", googleAnalytics);
        model.addObject("seoMeta", seoMeta);
        model.addObject("latestStats", latestStats);
        model.addObject("sourceRecipes", recipeRepository.getRecipesByIngredient(itemId));
        model.addObject("targetRecipes", recipeRepository.getRecipesByOutputItem(itemId));
        model.addObject("view", "details");

        return model;
    }

    private SeoMeta createSeoMeta(ListingStatistics latestStats) {
        SeoMeta seoMeta = new SeoMeta("details.title");
        seoMeta.setTitleArgs(new Object[] { latestStats.getItem().getName() });
        seoMeta.setImageUrl(latestStats.getItem().getIconUrl());
        seoMeta.setDescription("details.description");
        seoMeta.setDescriptionArgs(new Object[] {
                latestStats.getItem().getName(),
                formatPrice(latestStats.getBuyStatistics().getMaxPrice()),
                formatPrice(latestStats.getSellStatistics().getMinPrice()),
                formatPrice(latestStats.getProfit())
        });
        seoMeta.setKeywords(latestStats.getItem().getName());

        return seoMeta;
    }

    private String formatPrice(int coins) {
        Price price = Price.valueOf(coins);

        StringBuilder sb = new StringBuilder();
        if (price.getGoldCoins() > 0) {
            sb.append(price.getGoldCoins()).append("G");
        }
        if (price.getSilverCoins() > 0) {
            if (price.getGoldCoins() > 0) {
                sb.append(' ');
            }
            sb.append(price.getSilverCoins()).append("S");
        }
        if (price.getCopperCoins() > 0) {
            if (price.getGoldCoins() > 0 || price.getSilverCoins() > 0) {
                sb.append(' ');
            }
            sb.append(price.getCopperCoins()).append("C");
        }


        return sb.toString();
    }
    */
}
