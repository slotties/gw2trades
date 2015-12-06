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

    @Override
    public int hashCode() {
        return field.hashCode() ^ Boolean.hashCode(descending);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof Order &&
                ((Order) obj).field.equals(this.field) &&
                ((Order) obj).descending == this.descending;
    }
}
