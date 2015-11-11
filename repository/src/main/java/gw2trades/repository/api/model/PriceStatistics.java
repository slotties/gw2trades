package gw2trades.repository.api.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class PriceStatistics {
    private int minPrice;
    private int maxPrice;
    private double average;
    private int median;
    private int totalAmount;

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

    public double getAverage() {
        return average;
    }

    public void setAverage(double average) {
        this.average = average;
    }

    public int getMedian() {
        return median;
    }

    public void setMedian(int median) {
        this.median = median;
    }

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(int totalAmount) {
        this.totalAmount = totalAmount;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof PriceStatistics) {
            PriceStatistics other = (PriceStatistics) obj;
            return other.minPrice == this.minPrice &&
                    other.maxPrice == this.maxPrice &&
                    other.average == this.average &&
                    other.median == this.median &&
                    other.totalAmount == this.totalAmount;
        }

        return false;
    }

    @Override
    public int hashCode() {
        int hash = this.minPrice;
        hash = (31 * this.maxPrice) ^ hash;
        hash = (31 * (int) this.average) ^ hash;
        hash = (31 * this.median) ^ hash;
        hash = (31 * this.totalAmount) ^ hash;

        return hash;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb
                .append("[")
                .append("min=").append(this.minPrice)
                .append(",max=").append(this.maxPrice)
                .append(",avg=").append(this.average)
                .append(",median=").append(this.median)
                .append(",total#=").append(this.totalAmount)
                .append("]");

        return sb.toString();
    }
}
