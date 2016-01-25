package gw2trades.repository.lucene;

import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.api.model.Recipe;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class LuceneRecipeRepository implements RecipeRepository {
    private String indexDir;
    private IndexReader indexReader;

    public LuceneRecipeRepository(String indexDir, boolean enableReading) throws IOException {
        this.indexDir = indexDir;
        if (enableReading) {
            this.indexReader = openIndexReader();
        }
    }

    private IndexReader openIndexReader() throws IOException {
        return DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
    }

    @Override
    public void store(Collection<Recipe> recipes) throws IOException {
        IndexWriter indexWriter = openIndexWriter();
        try {
            for (Recipe recipe : recipes) {
                Document doc = createDoc(recipe);
                indexWriter.addDocument(doc);
            }

            indexWriter.commit();
        } finally {
            indexWriter.close();
        }
    }

    private IndexWriter openIndexWriter() throws IOException {
        // TODO: correct analyzers
        Directory directory = FSDirectory.open(Paths.get(indexDir));
        StandardAnalyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
        iwc.setOpenMode(IndexWriterConfig.OpenMode.CREATE);

        return new IndexWriter(directory, iwc);
    }

    private Document createDoc(Recipe recipe) {
        Document doc = new Document();
        doc.add(new StringField("recipeId", Integer.toString(recipe.getId()), Field.Store.YES));

        doc.add(new StringField("outputItemId", Integer.toString(recipe.getOutputItemId()), Field.Store.YES));
        doc.add(new TextField("type", recipe.getType(), Field.Store.YES));
        doc.add(new SortedDocValuesField("type", new BytesRef(recipe.getType())));

        doc.add(new IntField("minRating", recipe.getMinRating(), IntField.TYPE_STORED));
        if (recipe.getDisciplines() != null) {
            for (String discipline : recipe.getDisciplines()) {
                doc.add(new StringField("discipline", discipline, Field.Store.YES));
            }
        }

        doc.add(new TextField("outputItemName", recipe.getOutputItemName(), Field.Store.YES));
        doc.add(new SortedDocValuesField("outputItemName", new BytesRef(recipe.getOutputItemName())));

        List<Recipe.Ingredient> ingredients = recipe.getIngredients();
        if (ingredients != null) {
            doc.add(new IntField("ingredientsCount", ingredients.size(), IntField.TYPE_STORED));
            for (int i = 0; i < ingredients.size(); i++) {
                Recipe.Ingredient ingredient = ingredients.get(i);
                doc.add(new StringField("ingredients", Integer.toString(ingredient.getItemId()), Field.Store.YES));
                doc.add(new IntField("ingredient" + i + "_id", ingredient.getItemId(), IntField.TYPE_STORED));
                doc.add(new IntField("ingredient" + i + "_count", ingredient.getCount(), IntField.TYPE_STORED));
                doc.add(new TextField("ingredient" + i + "_name", ingredient.getName(), Field.Store.YES));
                doc.add(new SortedDocValuesField("ingredient" + i + "_name", new BytesRef(ingredient.getName())));
            }
        } else {
            doc.add(new IntField("ingredientsCount", 0, IntField.TYPE_STORED));
        }

        return doc;
    }

    @Override
    public Recipe getRecipe(int id) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);
        Query query = new TermQuery(new Term("recipeId", Integer.toString(id)));
        TopDocs topDocs = searcher.search(query, 1);
        if (topDocs.totalHits < 1) {
            return null;
        }

        Document doc = indexReader.document(topDocs.scoreDocs[0].doc);
        return readRecipe(doc);
    }

    @Override
    public Collection<Recipe> getRecipesByIngredient(int itemId) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);
        Query query = new TermQuery(new Term("ingredients", Integer.toString(itemId)));
        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

        List<Recipe> recipes = new ArrayList<>(topDocs.totalHits);
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            Document doc = indexReader.document(topDocs.scoreDocs[i].doc);
            if (doc != null) {
                recipes.add(readRecipe(doc));
            }
        }

        return recipes;
    }

    @Override
    public Collection<Recipe> getRecipesByOutputItem(int itemId) throws IOException {
        IndexSearcher searcher = new IndexSearcher(this.indexReader);
        Query query = new TermQuery(new Term("outputItemId", Integer.toString(itemId)));
        TopDocs topDocs = searcher.search(query, Integer.MAX_VALUE);

        List<Recipe> recipes = new ArrayList<>(topDocs.totalHits);
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            Document doc = indexReader.document(topDocs.scoreDocs[i].doc);
            if (doc != null) {
                recipes.add(readRecipe(doc));
            }
        }

        return recipes;
    }

    private Recipe readRecipe(Document document) {
        Recipe recipe = new Recipe();
        recipe.setId(Integer.valueOf(document.get("recipeId")));
        recipe.setOutputItemId(Integer.valueOf(document.get("outputItemId")));
        recipe.setType(document.get("type"));
        recipe.setOutputItemName(document.get("outputItemName"));
        recipe.setMinRating(Integer.valueOf(document.get("minRating")));

        recipe.setDisciplines(Arrays.asList(document.getValues("discipline")));

        int ingredientsCount = Integer.valueOf(document.get("ingredientsCount"));
        List<Recipe.Ingredient> ingredients = new ArrayList<>(ingredientsCount);
        for (int i = 0; i < ingredientsCount; i++) {
            Recipe.Ingredient ingredient = new Recipe.Ingredient();
            ingredient.setItemId(Integer.valueOf(document.get("ingredient" + i + "_id")));
            ingredient.setCount(Integer.valueOf(document.get("ingredient" + i + "_count")));
            ingredient.setName(document.get("ingredient" + i + "_name"));
            ingredients.add(ingredient);
        }
        recipe.setIngredients(ingredients);

        return recipe;
    }

    @Override
    public void close() throws IOException {
        if (indexReader != null) {
            indexReader.close();
        }
    }

    @Override
    public void reopen() throws IOException {
        close();
        this.indexReader = openIndexReader();
    }
}
