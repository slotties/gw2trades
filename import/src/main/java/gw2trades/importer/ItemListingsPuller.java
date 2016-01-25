package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ItemListingsPuller implements Callable<List<ItemListings>> {
    private static final Logger LOGGER = LogManager.getLogger(ItemListingsPuller.class);

    private final TradingPost tradingPost;
    private final List<Integer> itemIdsChunk;
    private final Map<Integer, Item> items;

    public ItemListingsPuller(TradingPost tradingPost, List<Integer> itemIdsChunk, Map<Integer, Item> items) {
        this.tradingPost = tradingPost;
        this.itemIdsChunk = itemIdsChunk;
        this.items = items;
    }

    @Override
    public List<ItemListings> call() throws Exception {
        try {
            List<ItemListings> listings = tradingPost.listings(itemIdsChunk);

            for (ItemListings listing : listings) {
                Item item = items.get(listing.getItemId());
                if (item != null) {
                    listing.setItem(item);
                }
            }

            return listings;
        } catch (IOException e) {
            LOGGER.error("Could not import item ids {}", itemIdsChunk, e);
            return Collections.emptyList();
        }
    }
}
