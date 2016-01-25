package gw2trades.repository.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Recipe {
    private String type;
    @JsonProperty("output_item_id")
    private int outputItemId;
    private String outputItemName;
    private List<Ingredient> ingredients;
    private int id;
    private List<String> disciplines;
    @JsonProperty("min_rating")
    private int minRating;

    public List<String> getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(List<String> disciplines) {
        this.disciplines = disciplines;
    }

    public int getMinRating() {
        return minRating;
    }

    public void setMinRating(int minRating) {
        this.minRating = minRating;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getOutputItemId() {
        return outputItemId;
    }

    public void setOutputItemId(int outputItemId) {
        this.outputItemId = outputItemId;
    }

    public List<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(List<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getOutputItemName() {
        return outputItemName;
    }

    public void setOutputItemName(String outputItemName) {
        this.outputItemName = outputItemName;
    }

    @Override
    public int hashCode() {
        return Integer.hashCode(this.id);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Recipe && ((Recipe) obj).id == this.id;
    }

    @Override
    public String toString() {
        return "Recipe: " + this.id;
    }

    public static class Ingredient {
        @JsonProperty("item_id")
        private int itemId;
        private int count;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getItemId() {
            return itemId;
        }

        public void setItemId(int itemId) {
            this.itemId = itemId;
        }

        public int getCount() {
            return count;
        }

        public void setCount(int count) {
            this.count = count;
        }
    }
}
