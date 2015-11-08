package gw2trades.repository.api.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class PriceStatistics {

    private int minPrice;
    private int maxPrice;

    // TODO: average, median, amount of listings for sale, amount of listings to buy

    public int getMinPrice() {
        return minPrice;
    }

    public void setMinPrice(int minPrice) {
        this.minPrice = minPrice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }
}
