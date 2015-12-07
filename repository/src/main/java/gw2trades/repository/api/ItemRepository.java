package gw2trades.repository.api;

import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.SearchResult;

import java.io.IOException;
import java.time.LocalDateTime;
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
     * Returns all statistics.
     * @return an unordered collection of statistics
     * @throws IOException in case the repository could not be written
     */
    SearchResult<ListingStatistics> listStatistics(Query query, Order order, int fromPage, int toPage) throws IOException;

    /**
     * Returns the history for an item ordered by its timestamp (oldest to newest).
     * @param itemId an item ID
     * @param from the timestamp to begin at (inclusive)
     * @param to the timestamp to end at (inclusive)
     * @return statistics
     * @throws IOException in case the repository could not be written
     */
    List<ListingStatistics> getHistory(int itemId, LocalDateTime from, LocalDateTime to) throws IOException;

    /**
     * Returns the latest/newest statistics for an item.
     * @param itemId an item ID
     * @return the newest statistics or null, in case this item has none yet.
     * @throws IOException in case the repository could not be written
     */
    ListingStatistics latestStatistics(int itemId) throws IOException;

    /**
     * Closes the repository.
     * @throws IOException in case the repository could not be closed
     */
    void close() throws IOException;

    /**
     * Reopens the repository.
     * @throws IOException in case the repository could not be closed or opened.
     */
    void reopen() throws IOException;
}
