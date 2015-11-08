package gw2trades.repository.api;

import gw2trades.repository.api.model.ItemListings;

import java.io.IOException;
import java.util.Collection;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public interface ItemRepository {
    /**
     * Stores all given listings in this repository.
     * @param listings one or many listings
     * @param timestamp a point on the timeline to store the listings at (important for providing timelines of price changes)
     */
    void store(Collection<ItemListings> listings, long timestamp) throws IOException;
}
