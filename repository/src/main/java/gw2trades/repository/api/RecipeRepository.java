package gw2trades.repository.api;

import gw2trades.repository.api.model.Recipe;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public interface RecipeRepository {
    void store(Collection<Recipe> recipes) throws IOException;

    Recipe getRecipe(int id) throws IOException;
    Collection<Recipe> getRecipesByIngredient(int itemId) throws IOException;

    void close() throws IOException;

    void reopen() throws IOException;
}
