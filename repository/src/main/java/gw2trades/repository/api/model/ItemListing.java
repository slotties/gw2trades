package gw2trades.repository.api.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@JsonIgnoreProperties({ "listings" })
public class ItemListing {
    private int quantity;
    @JsonProperty("unit_price")
    private int unitPrice;

    public int getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ItemListing) {
            return this.unitPrice == ((ItemListing) obj).getUnitPrice() &&
                    this.quantity == ((ItemListing) obj).getQuantity();

        }

        return false;
    }

    @Override
    public String toString() {
        return "[" +
                "unitPrice:" + this.unitPrice + "," +
                "quantity:" + this.quantity +
                "]";
    }
}
