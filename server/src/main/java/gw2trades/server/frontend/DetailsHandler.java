package gw2trades.server.frontend;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.Recipe;
import gw2trades.server.VelocityRenderer;
import gw2trades.server.frontend.exception.ItemNotFoundException;
import gw2trades.server.model.GoogleAnalytics;
import gw2trades.server.model.Price;
import gw2trades.server.model.SeoMeta;
import io.vertx.core.Handler;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.ext.web.RoutingContext;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class DetailsHandler implements Handler<RoutingContext> {
    private final ItemRepository itemRepository;
    private final RecipeRepository recipeRepository;
    private final VelocityRenderer renderer;

    public DetailsHandler(ItemRepository itemRepository, RecipeRepository recipeRepository, VelocityRenderer renderer) {
        this.itemRepository = itemRepository;
        this.recipeRepository = recipeRepository;
        this.renderer = renderer;
    }

    private int resolveItemId(HttpServerRequest request) {
        String uri = request.uri();
        int idStartIdx = uri.lastIndexOf('/');
        int idEndIdx = uri.lastIndexOf('.');

        if (idStartIdx < 0 || idEndIdx < 0 || idEndIdx < idStartIdx) {
            return -1;
        }

        try {
            return Integer.valueOf(uri.substring(idStartIdx + 1, idEndIdx));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    @Override
    public void handle(RoutingContext event) {
        int itemId = resolveItemId(event.request());
        if (itemId < 0) {
            event.fail(new ItemNotFoundException());
            return;
        }

        ListingStatistics latestStats;
        Collection<Recipe> recipesByIngredient;
        Collection<Recipe> recipesByOutput;
        try {
            latestStats = itemRepository.latestStatistics(itemId);
            if (latestStats == null) {
                event.fail(new ItemNotFoundException());
                return;
            }

            recipesByIngredient = recipeRepository.getRecipesByIngredient(itemId);
            recipesByOutput = recipeRepository.getRecipesByOutputItem(itemId);
        } catch (IOException e) {
            event.fail(new ItemNotFoundException());
            return;
        }

        SeoMeta seoMeta = createSeoMeta(latestStats);
        GoogleAnalytics googleAnalytics = new GoogleAnalytics();
        googleAnalytics.getDimensions().add(Integer.toString(latestStats.getItem().getItemId()));

        Map<String, Object> ctx = new HashMap<>();
        ctx.put("googleAnalytics", googleAnalytics);
        ctx.put("seoMeta", seoMeta);
        ctx.put("latestStats", latestStats);
        ctx.put("sourceRecipes", recipesByIngredient);
        ctx.put("targetRecipes", recipesByOutput);
        ctx.put("view", "details");

        event.response().end(renderer.render("frame.vm", event, ctx));
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
}
