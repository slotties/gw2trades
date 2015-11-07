package gw2trades.importer.dao

import com.fasterxml.jackson.databind.ObjectMapper
import gw2trades.importer.http.ApiClient
import gw2trades.repository.api.model.ItemListing
import spock.lang.Specification
import spock.lang.Subject

class TradingPostSpec extends Specification {
    @Subject
    TradingPost tradingPost = new TradingPost()

    ApiClient apiClient = Mock(ApiClient)
    ObjectMapper objectMapper = new ObjectMapper()

    def setup() {
        tradingPost.apiClient = apiClient
        tradingPost.objectMapper = objectMapper
    }

    def listItemIds() {
        given:
        apiClient.get(TradingPost.URL_COMMERCE_LISTINGS) >> "[1,5,100,31337]"

        when:
        def itemIds = tradingPost.listItemIds()

        then:
        itemIds == [ 1, 5, 100, 31337 ]
    }

    def listings() {
        given:
        apiClient.get(TradingPost.URL_COMMERCE_LISTINGS + "?ids=1,2,3") >> "[" +
                "  {" +
                "    \"id\": 19709," +
                "    \"buys\": [" +
                "      { \"listings\":  9, \"unit_price\":  1, \"quantity\":  335 }," +
                "      { \"listings\": 81, \"unit_price\":  8, \"quantity\": 3092 }" +
                "    ]," +
                "    \"sells\": [" +
                "      { \"listings\":  2, \"unit_price\": 63, \"quantity\":  499 }," +
                "      { \"listings\":  3, \"unit_price\": 64, \"quantity\":  289 }" +
                "    ]" +
                "  }," +
                "  {" +
                "    \"id\": 19684," +
                "    \"buys\": [" +
                "      { \"listings\": 23, \"unit_price\":  1, \"quantity\": 1962 }," +
                "      { \"listings\":  1, \"unit_price\":  5, \"quantity\":   45 }" +
                "    ]," +
                "    \"sells\": [" +
                "      { \"listings\":  4, \"unit_price\": 98, \"quantity\":  951 }," +
                "      { \"listings\":  1, \"unit_price\": 99, \"quantity\":  211 }" +
                "    ]" +
                "  }" +
                "]"

        when:
        def listings = tradingPost.listings([ 1, 2, 3 ])

        then:
        listings.size() == 2
        listings.get(0).itemId == 19709
        listings.get(0).buys != null
        listings.get(0).buys.size() == 2
        listings.get(0).buys.contains(new ItemListing(unitPrice: 1, quantity: 335))
        listings.get(0).buys.contains(new ItemListing(unitPrice: 8, quantity: 3092))
        listings.get(0).sells != null
        listings.get(0).sells.size() == 2
        listings.get(0).sells.contains(new ItemListing(unitPrice: 63, quantity: 499))
        listings.get(0).sells.contains(new ItemListing(unitPrice: 64, quantity: 289))

        listings.get(1).itemId == 19684
        listings.get(1).buys != null
        listings.get(1).buys.size() == 2
        listings.get(1).buys.contains(new ItemListing(unitPrice: 1, quantity: 1962))
        listings.get(1).buys.contains(new ItemListing(unitPrice: 5, quantity: 45))
        listings.get(1).sells != null
        listings.get(1).sells.size() == 2
        listings.get(1).sells.contains(new ItemListing(unitPrice: 98, quantity: 951))
        listings.get(1).sells.contains(new ItemListing(unitPrice: 99, quantity: 211))
    }

    def listingsWithSingleItem() {
        given:
        apiClient.get(TradingPost.URL_COMMERCE_LISTINGS + "?ids=1") >> "[" +
                "  {" +
                "    \"id\": 19709," +
                "    \"buys\": [" +
                "      { \"listings\":  9, \"unit_price\":  1, \"quantity\":  335 }," +
                "      { \"listings\": 81, \"unit_price\":  8, \"quantity\": 3092 }" +
                "    ]," +
                "    \"sells\": [" +
                "      { \"listings\":  2, \"unit_price\": 63, \"quantity\":  499 }," +
                "      { \"listings\":  3, \"unit_price\": 64, \"quantity\":  289 }" +
                "    ]" +
                "  }" +
                "]"

        when:
        def listings = tradingPost.listings([ 1 ])

        then:
        listings.size() == 1
        listings.get(0).itemId == 19709
        listings.get(0).buys != null
        listings.get(0).buys.size() == 2
        listings.get(0).buys.contains(new ItemListing(unitPrice: 1, quantity: 335))
        listings.get(0).buys.contains(new ItemListing(unitPrice: 8, quantity: 3092))
        listings.get(0).sells != null
        listings.get(0).sells.size() == 2
        listings.get(0).sells.contains(new ItemListing(unitPrice: 63, quantity: 499))
        listings.get(0).sells.contains(new ItemListing(unitPrice: 64, quantity: 289))
    }
}
