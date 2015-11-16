package gw2trades.server.frontend.sorters;

import gw2trades.repository.api.model.ListingStatistics;

import java.util.Comparator;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class SellersByMinPrice implements Comparator<ListingStatistics> {
    @Override
    public int compare(ListingStatistics o1, ListingStatistics o2) {
        return Double.compare(
                o1.getSellStatistics().getMinPrice(),
                o2.getSellStatistics().getMinPrice()
        );
    }
}
