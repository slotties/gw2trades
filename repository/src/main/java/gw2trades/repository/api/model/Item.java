package gw2trades.repository.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Item {
    private String name;
    @JsonProperty("id")
    private int itemId;
    @JsonProperty("icon")
    private String iconUrl;

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

    public String getIconUrl() {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl) {
        this.iconUrl = iconUrl;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Item) {
            return this.itemId == ((Item) obj).itemId;
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.itemId;
    }

    @Override
    public String toString() {
        return this.itemId + ":" + this.name;
    }
}
