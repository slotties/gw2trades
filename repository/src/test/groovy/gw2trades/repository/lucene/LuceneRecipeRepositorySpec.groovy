package gw2trades.repository.lucene

import gw2trades.repository.api.model.Recipe
import org.apache.commons.io.FileUtils
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import spock.lang.Specification
import spock.lang.Subject

import java.nio.file.Paths

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class LuceneRecipeRepositorySpec extends Specification {
    @Subject
    LuceneRecipeRepository repository

    File tmpDir

    def setup() {
        tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()))
    }

    def cleanup() {
        FileUtils.deleteDirectory(tmpDir)
    }

    def store() {
        given:
        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), false)
        def recipe = new Recipe(
                id: 123,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 1, count: 2, name: "bla"),
                        new Recipe.Ingredient(itemId: 3, count: 4, name: "blub"),
                ]
        )

        when:
        repository.store([ recipe ])
        repository.close()

        def indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(tmpDir.getAbsolutePath())))
        def document = indexReader.document(indexReader.maxDoc() - 1)
        indexReader.close()

        then:
        document.get("recipeId") == "123"
        document.get("outputItemId") == "456"
        document.get("type") == "foo"
        document.get("outputItemName") == "xxx"
        document.get("ingredientsCount") == "2"
        document.getValues("ingredients") == [ "1", "3" ]
        document.get("ingredient0_id") == "1"
        document.get("ingredient0_count") == "2"
        document.get("ingredient0_name") == "bla"
        document.get("ingredient1_id") == "3"
        document.get("ingredient1_count") == "4"
        document.get("ingredient1_name") == "blub"
    }

    def getRecipe() {
        given:
        def recipe = new Recipe(
                id: 123,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 1, count: 2, name: "bla"),
                        new Recipe.Ingredient(itemId: 3, count: 4, name: "blub"),
                ]
        )

        when:
        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), false)
        repository.store([ recipe ])
        repository.close()

        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), true)
        def receivedRecipe = repository.getRecipe(123)
        repository.close()

        then:
        receivedRecipe != null
        receivedRecipe.id == 123
        receivedRecipe.outputItemId == 456
        receivedRecipe.outputItemName == "xxx"
        receivedRecipe.type == "foo"
        receivedRecipe.ingredients.size() == 2
        receivedRecipe.ingredients.get(0).itemId == 1
        receivedRecipe.ingredients.get(0).count == 2
        receivedRecipe.ingredients.get(0).name == "bla"
        receivedRecipe.ingredients.get(1).itemId == 3
        receivedRecipe.ingredients.get(1).count == 4
        receivedRecipe.ingredients.get(1).name == "blub"
    }

    def getRecipesByIngredient() {
        given:
        def recipeMatch1 = new Recipe(
                id: 123,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 1, count: 2, name: "bla"),
                        new Recipe.Ingredient(itemId: 3, count: 4, name: "bla"),
                ]
        )
        def recipeMatch2 = new Recipe(
                id: 456,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 3, count: 4, name: "bla"),
                        new Recipe.Ingredient(itemId: 1, count: 2, name: "bla"),
                ]
        )
        def recipeNoMatch = new Recipe(
                id: 789,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 3, count: 4, name: "bla"),
                ]
        )

        when:
        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), false)
        repository.store([ recipeMatch1, recipeMatch2, recipeNoMatch ])
        repository.close()

        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), true)
        def recipes = repository.getRecipesByIngredient(1)
        repository.close()

        then:
        assert recipes.size() == 2
        assert recipes.contains(recipeMatch1)
        assert recipes.contains(recipeMatch2)
    }

    def getRecipesByOutputItem() {
        given:
        def recipeMatch = new Recipe(
                id: 123,
                outputItemId: 456,
                outputItemName: "xxx",
                type: "foo",
        )
        def recipeNoMatch = new Recipe(
                id: 456,
                outputItemId: 789,
                outputItemName: "xxx",
                type: "foo",
        )

        when:
        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), false)
        repository.store([ recipeMatch, recipeNoMatch ])
        repository.close()

        repository = new LuceneRecipeRepository(tmpDir.getAbsolutePath(), true)
        def recipes = repository.getRecipesByOutputItem(456)
        repository.close()

        then:
        assert recipes.size() == 1
        assert recipes.contains(recipeMatch)
    }
}
