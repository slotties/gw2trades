package gw2trades.repository.api;

import gw2trades.repository.api.model.ItemListings;

import java.util.Collection;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public interface ItemRepository {
    /**
     * TODO
     * @param listings
     * @param timestamp
     */
    void store(Collection<ItemListings> listings, long timestamp);
}
