package gw2trades.importer.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@JsonIgnoreProperties({ "listings" })
public class CommerceListing {
    @JsonProperty("unit_price")
    private int unitPrice;
    private int quantity;

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setUnitPrice(int unitPrice) {
        this.unitPrice = unitPrice;
    }

    public int getUnitPrice() {
        return unitPrice;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof CommerceListing) {
            return this.unitPrice == ((CommerceListing) obj).getUnitPrice() &&
                    this.quantity == ((CommerceListing) obj).getQuantity();

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
