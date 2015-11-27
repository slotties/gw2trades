package gw2trades.repository.api;

import gw2trades.repository.api.model.Item;
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
     * @throws IOException in case the repository could not be written
     */
    void store(Collection<ItemListings> listings, long timestamp) throws IOException;

    /**
     * Stores all given items.
     * @param items items
     * @throws IOException in case the repository could not be written
     */
    void store(Collection<Item> items) throws IOException;

    /**
     * Returns an item by its ID.
     * @param itemId an item ID
     * @return null when the item was not found
     * @throws IOException in case the repository could not be written
     */
    Item getItem(int itemId) throws IOException;

    /**
     * Returns all statistics.
     * @return an unordered collection of statistics
     * @throws IOException in case the repository could not be written
     */
    Collection<ListingStatistics> listStatistics() throws IOException;

    /**
     * Queries for statistics.
     * @param  query a query, must not be null
     * @return all statistics matching the given query, never null.
     * @throws IOException in case the repository could not be written
     */
    Collection<ListingStatistics> queryStatistics(Query query) throws IOException;

    /**
     * Returns the history for an item ordered by its timestamp (oldest to newest).
     * @param itemId an item ID
     * @param fromTimestamp the timestamp to begin at (inclusive)
     * @param toTimestamp the timestamp to end at (inclusive)
     * @return statistics
     * @throws IOException in case the repository could not be written
     */
    List<ListingStatistics> getHistory(int itemId, long fromTimestamp, long toTimestamp) throws IOException;

    /**
     * Returns the latest/newest statistics for an item.
     * @param itemId an item ID
     * @return the newest statistics or null, in case this item has none yet.
     * @throws IOException in case the repository could not be written
     */
    ListingStatistics latestStatistics(int itemId) throws IOException;

    void close() throws IOException;
}
