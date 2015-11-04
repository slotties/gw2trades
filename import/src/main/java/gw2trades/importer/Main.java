package gw2trades.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.dao.TradingPost;
import gw2trades.importer.http.ApiClient;
import gw2trades.importer.model.CommerceListings;

import java.util.List;

/**
 * TODO
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ApiClient apiClient = new ApiClient();
        ObjectMapper objectMapper = new ObjectMapper();

        TradingPost tradingPost = new TradingPost();
        tradingPost.setApiClient(apiClient);
        tradingPost.setObjectMapper(objectMapper);

        List<Integer> itemIds = tradingPost.listItemIds();
        int batchSize = 50;
        int batches = (int) Math.ceil((float) itemIds.size() / (float) batchSize);
        for (int i = 0; i < batches; i++) {
            System.out.printf("Pulling batch %d of %d (total %d items)...\n",
                    i, batches, itemIds.size());

            // TODO: implement parallel processing to improve speed
            List<Integer> itemIdBatch = itemIds.subList(i * batchSize, Math.min(itemIds.size() - 1, (i + 1) * batchSize));
            List<CommerceListings> listings = tradingPost.listings(itemIdBatch);
            // TODO: import listings somewhere
            for (CommerceListings listing : listings) {
                System.out.printf("%d: %d buyers, %d sellers\n",
                        listing.getItemId(),
                        listing.getBuys().size(), listing.getSells().size());
            }
        }
    }
}
