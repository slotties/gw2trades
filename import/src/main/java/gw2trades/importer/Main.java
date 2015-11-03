package gw2trades.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.dao.TradingPost;
import gw2trades.importer.http.ApiClient;

import java.util.Collection;

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

        Collection<Integer> itemIds = tradingPost.listItemIds();
        System.out.println(itemIds);
        // TODO: pull item pricings and listings ...
    }
}
