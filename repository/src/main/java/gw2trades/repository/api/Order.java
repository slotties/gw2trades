package gw2trades.repository.api;

/**
 * This class represents a sort order state, that is a combination of a field to sort and the direction (ascending, descending).
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Order {
    private final String field;
    private final boolean descending;

    public static Order by(String field, boolean descending) {
        return new Order(field, descending);
    }

    private Order(String field, boolean descending) {
        this.field = field;
        this.descending = descending;
    }

    public boolean isDescending() {
        return descending;
    }

    public String getField() {
        return field;
    }
}
