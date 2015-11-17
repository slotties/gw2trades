package gw2trades.repository.api;

import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.api.model.ListingStatistics;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

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

    /**
     * TODO
     * @return
     * @throws IOException
     */
    Collection<ListingStatistics> listStatistics() throws IOException;

    /**
     * TODO
     * @param itemId
     * @param fromTimestamp
     * @param toTimestamp
     * @return
     * @throws IOException
     */
    List<ListingStatistics> getHistory(int itemId, long fromTimestamp, long toTimestamp) throws IOException;

    /**
     * TODO
     * @param itemId
     * @return
     * @throws IOException
     */
    ListingStatistics latestStatistics(int itemId) throws IOException;
}
