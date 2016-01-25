package gw2trades.importer;

import gw2trades.importer.dao.TradingPost;
import gw2trades.repository.api.model.Item;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ItemPuller implements Callable<List<Item>> {
    private static final Logger LOGGER = LogManager.getLogger(ItemListingsPuller.class);

    private final TradingPost tradingPost;
    private final List<Integer> itemIdsChunk;

    public ItemPuller(TradingPost tradingPost, List<Integer> itemIdsChunk) {
        this.tradingPost = tradingPost;
        this.itemIdsChunk = itemIdsChunk;
    }

    @Override
    public List<Item> call() throws Exception {
        try {
            return tradingPost.listItems(itemIdsChunk);
        } catch (IOException e) {
            LOGGER.error("Could not import item ids {}", itemIdsChunk, e);
            return Collections.emptyList();
        }
    }
}
