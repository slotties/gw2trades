package gw2trades.importer.dao;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.http.ApiClient;
import gw2trades.repository.api.model.Item;
import gw2trades.repository.api.model.ItemListings;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The trading post provides access to the Guild Wars 2 trading post. Currently this just includes reading access.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class TradingPost {
    private static final String URL_COMMERCE_LISTINGS = "https://api.guildwars2.com/v2/commerce/listings";
    private static final String URL_ITEMS = "https://api.guildwars2.com/v2/items";

    private ApiClient apiClient;
    private ObjectMapper objectMapper;

    public void setApiClient(ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public List<Integer> listItemIds() throws IOException {
        String content = apiClient.get(URL_COMMERCE_LISTINGS);
        List<Integer> itemIds = objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(List.class, Integer.class));
        return itemIds;
    }

    public List<ItemListings> listings(List<Integer> itemIds) throws IOException {
        String idsStr = itemIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String url = URL_COMMERCE_LISTINGS + "?ids=" + idsStr;
        String content = apiClient.get(url);
        List<ItemListings> commerceListings = objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(List.class, ItemListings.class));

        return commerceListings;
    }

    public List<Item> listItems(List<Integer> itemIds) throws IOException {
        String idsStr = itemIds.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String url = URL_ITEMS + "?ids=" + idsStr;
        String content = apiClient.get(url);
        List<Item> items = objectMapper.readValue(content, objectMapper.getTypeFactory().constructCollectionType(List.class, Item.class));

        return items;
    }
}
