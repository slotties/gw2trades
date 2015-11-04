package gw2trades.importer.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class CommerceListings {
    @JsonProperty(value = "id")
    private int itemId;
    private List<CommerceListing> buys;
    private List<CommerceListing> sells;

    public List<CommerceListing> getSells() {
        return sells;
    }

    public void setSells(List<CommerceListing> sells) {
        this.sells = sells;
    }

    public List<CommerceListing> getBuys() {
        return buys;
    }

    public void setBuys(List<CommerceListing> buys) {
        this.buys = buys;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public int getItemId() {
        return itemId;
    }
}
