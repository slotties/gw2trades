package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.Recipe;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class RecipePuller implements Callable<List<Recipe>> {
    private static final Logger LOGGER = LogManager.getLogger(RecipePuller.class);

    private final TradingPost tradingPost;
    private final List<Integer> ids;
    private final Map<Integer, Item> items;

    public RecipePuller(TradingPost tradingPost, List<Integer> ids, Map<Integer, Item> items) {
        this.tradingPost = tradingPost;
        this.ids = ids;
        this.items = items;
    }

    @Override
    public List<Recipe> call() throws Exception {
        try {
            List<Recipe> recipes = tradingPost.listRecipes(ids);

            // Register all listings.
            for (Recipe recipe : recipes) {
                recipe.setOutputItemName(resolveName(recipe.getOutputItemId()));
                List<Recipe.Ingredient> ingredients = recipe.getIngredients();
                if (ingredients != null) {
                    ingredients.forEach(ingredient -> ingredient.setName(resolveName(ingredient.getItemId())));
                }
            }

            return recipes;
        } catch (IOException e) {
            LOGGER.error("Could not import recipes {}", ids, e);
            return Collections.emptyList();
        }
    }

    private String resolveName(int itemId) {
        Item item = items.get(itemId);
        return item != null ? item.getName() : Integer.toString(itemId);
    }
}
