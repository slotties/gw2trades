package gw2trades.repository.api.model;

import java.util.Objects;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ListingStatistics {
    private int itemId;
    private long timestamp;
    private PriceStatistics buyStatistics;
    private PriceStatistics sellStatistics;

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public long getTimestamp() {
        return timestamp;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ListingStatistics) {
            ListingStatistics other = (ListingStatistics) obj;
            return other.itemId == this.itemId &&
                    Objects.equals(this.buyStatistics, other.buyStatistics) &&
                    Objects.equals(this.sellStatistics, other.sellStatistics);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return this.itemId ^ Objects.hashCode(this.buyStatistics) ^ Objects.hashCode(this.sellStatistics);
    }
}
