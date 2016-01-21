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
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 1, count: 2),
                        new Recipe.Ingredient(itemId: 3, count: 4),
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
        document.get("ingredientsCount") == "2"
        document.getValues("ingredients") == [ "1", "3" ]
        document.get("ingredient0_id") == "1"
        document.get("ingredient0_count") == "2"
        document.get("ingredient1_id") == "3"
        document.get("ingredient1_count") == "4"
    }

    def getRecipe() {
        given:
        def recipe = new Recipe(
                id: 123,
                outputItemId: 456,
                type: "foo",
                ingredients: [
                        new Recipe.Ingredient(itemId: 1, count: 2),
                        new Recipe.Ingredient(itemId: 3, count: 4),
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
        receivedRecipe.type == "foo"
        receivedRecipe.ingredients.size() == 2
        receivedRecipe.ingredients.get(0).itemId == 1
        receivedRecipe.ingredients.get(0).count == 2
        receivedRecipe.ingredients.get(1).itemId == 3
        receivedRecipe.ingredients.get(1).count == 4
    }
}
