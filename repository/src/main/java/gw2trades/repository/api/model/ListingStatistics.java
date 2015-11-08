package gw2trades.repository.api.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ListingStatistics {
    private int itemId;
    private PriceStatistics buyStatistics;
    private PriceStatistics sellStatistics;

    public int getItemId() {
        return itemId;
    }

    public void setItemId(int itemId) {
        this.itemId = itemId;
    }

    public PriceStatistics getBuyStatistics() {
        return buyStatistics;
    }

    public void setBuyStatistics(PriceStatistics buyStatistics) {
        this.buyStatistics = buyStatistics;
    }

    public PriceStatistics getSellStatistics() {
        return sellStatistics;
    }

    public void setSellStatistics(PriceStatistics sellStatistics) {
        this.sellStatistics = sellStatistics;
    }
}
