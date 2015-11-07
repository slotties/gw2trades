package gw2trades.repository.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ItemListings {
    @JsonProperty("id")
    private int itemId;
    private List<ItemListing> buys;
    private List<ItemListing> sells;

    public List<ItemListing> getSells() {
        return sells;
    }

    public void setSells(List<ItemListing> sells) {
        this.sells = sells;
    }

    public List<ItemListing> getBuys() {
        return buys;
    }

    public void setBuys(List<ItemListing> buys) {
        this.buys = buys;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }
}
