package gw2trades.importer.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.http.ApiClient;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * The trading post provides access to the Guild Wars 2 trading post. Currently this just includes reading access.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class TradingPost {
    private static final String URL_COMMERCE_LISTINGS = "https://api.guildwars2.com/v2/commerce/listings";

    private ApiClient apiClient;
    private ObjectMapper objectMapper;

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Collection<Integer> listItemIds() throws IOException {
        String content = apiClient.get(URL_COMMERCE_LISTINGS);
        List<Integer> itemIds = objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
        return itemIds;
    }
}
