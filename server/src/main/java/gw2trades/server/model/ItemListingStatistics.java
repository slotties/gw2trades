package gw2trades.server.model;

import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ListingStatistics;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ItemListingStatistics {
    private final Item item;
    private final ListingStatistics statistics;

    public ItemListingStatistics(Item item, ListingStatistics statistics) {
        this.item = item;
        this.statistics = statistics;
    }

    public Item getItem() {
        return item;
    }

    public ListingStatistics getStatistics() {
        return statistics;
    }
}
