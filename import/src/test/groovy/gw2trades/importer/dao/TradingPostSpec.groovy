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

    def listItems() {
        given:
        apiClient.get(TradingPost.URL_ITEMS + "?ids=123,456") >> "[{\"name\":\"Zhos Maske\",\"description\":\"\",\"type\":\"Armor\",\"level\":80,\"rarity\":\"Exotic\",\"vendor_value\":330,\"default_skin\":95,\"game_types\":[\"Activity\",\"Wvw\",\"Dungeon\",\"Pve\"],\"flags\":[\"HideSuffix\",\"SoulBindOnUse\"],\"restrictions\":[],\"id\":123,\"chat_link\":\"[&AgF7AAAA]\",\"icon\":\"https://render.guildwars2.com/file/65A0C7367206E6CE4EC7C8CBE07EABAE0191BFBA/561548.png\",\"details\":{\"type\":\"Helm\",\"weight_class\":\"Medium\",\"defense\":97,\"infusion_slots\":[],\"infix_upgrade\":{\"attributes\":[{\"attribute\":\"Healing\",\"modifier\":60},{\"attribute\":\"Power\",\"modifier\":43},{\"attribute\":\"Toughness\",\"modifier\":43}]},\"suffix_item_id\":24696,\"secondary_suffix_item_id\":\"\"}},{\"name\":\"Berserkerhafte Verstärkte Schuppen-Stiefel des Rudels\",\"description\":\"\",\"type\":\"Armor\",\"level\":74,\"rarity\":\"Masterwork\",\"vendor_value\":122,\"default_skin\":74,\"game_types\":[\"Activity\",\"Wvw\",\"Dungeon\",\"Pve\"],\"flags\":[\"SoulBindOnUse\"],\"restrictions\":[],\"id\":456,\"chat_link\":\"[&AgHIAQAA]\",\"icon\":\"https://render.guildwars2.com/file/17C9516D0F0B2EBA616ED794C00F59411E931702/61014.png\",\"details\":{\"type\":\"Boots\",\"weight_class\":\"Heavy\",\"defense\":133,\"infusion_slots\":[],\"infix_upgrade\":{\"attributes\":[{\"attribute\":\"Power\",\"modifier\":33},{\"attribute\":\"Precision\",\"modifier\":23},{\"attribute\":\"CritDamage\",\"modifier\":23}]},\"suffix_item_id\":24700,\"secondary_suffix_item_id\":\"\"}}]"

        when:
        def items = tradingPost.listItems([ 123, 456 ])

        then:
        items.size() == 2
        items.get(0).itemId == 123
        items.get(0).name == "Zhos Maske"
        items.get(0).iconUrl == "https://render.guildwars2.com/file/65A0C7367206E6CE4EC7C8CBE07EABAE0191BFBA/561548.png"
        items.get(1).itemId == 456
        items.get(1).name == "Berserkerhafte Verstärkte Schuppen-Stiefel des Rudels"
        items.get(1).iconUrl == "https://render.guildwars2.com/file/17C9516D0F0B2EBA616ED794C00F59411E931702/61014.png"
    }

    def listItemsWithSingleItem() {
        given:
        apiClient.get(TradingPost.URL_ITEMS + "?ids=123") >> "[{\"name\":\"Zhos Maske\",\"description\":\"\",\"type\":\"Armor\",\"level\":80,\"rarity\":\"Exotic\",\"vendor_value\":330,\"default_skin\":95,\"game_types\":[\"Activity\",\"Wvw\",\"Dungeon\",\"Pve\"],\"flags\":[\"HideSuffix\",\"SoulBindOnUse\"],\"restrictions\":[],\"id\":123,\"chat_link\":\"[&AgF7AAAA]\",\"icon\":\"https://render.guildwars2.com/file/65A0C7367206E6CE4EC7C8CBE07EABAE0191BFBA/561548.png\",\"details\":{\"type\":\"Helm\",\"weight_class\":\"Medium\",\"defense\":97,\"infusion_slots\":[],\"infix_upgrade\":{\"attributes\":[{\"attribute\":\"Healing\",\"modifier\":60},{\"attribute\":\"Power\",\"modifier\":43},{\"attribute\":\"Toughness\",\"modifier\":43}]},\"suffix_item_id\":24696,\"secondary_suffix_item_id\":\"\"}}]"

        when:
        def items = tradingPost.listItems([ 123 ])

        then:
        items.size() == 1
        items.get(0).itemId == 123
        items.get(0).name == "Zhos Maske"
        items.get(0).iconUrl == "https://render.guildwars2.com/file/65A0C7367206E6CE4EC7C8CBE07EABAE0191BFBA/561548.png"
    }

    def listRecipeIds() {
        given:
        apiClient.get(TradingPost.URL_RECIPES) >> "[1,2,3,4,5,6,7]"

        when:
        def ids = tradingPost.listRecipeIds()

        then:
        ids == [ 1, 2,3,4,5,6,7 ]
    }

    def listRecipes() {
        given:
        apiClient.get(TradingPost.URL_RECIPES + "?ids=7319,8") >> "[{\"type\":\"RefinementEctoplasm\",\"output_item_id\":46742,\"output_item_count\":1,\"min_rating\":450,\"time_to_craft_ms\":5000,\"disciplines\":[\"Armorsmith\",\"Artificer\",\"Weaponsmith\",\"Huntsman\"],\"flags\":[\"AutoLearned\"],\"ingredients\":[{\"item_id\":19684,\"count\":50},{\"item_id\":19721,\"count\":1},{\"item_id\":46747,\"count\":10}],\"id\":7319,\"chat_link\":\"[&CZccAAA=]\"},{\"type\":\"Refinement\",\"output_item_id\":19747,\"output_item_count\":1,\"min_rating\":300,\"time_to_craft_ms\":2000,\"disciplines\":[\"Leatherworker\",\"Armorsmith\",\"Tailor\",\"Scribe\"],\"flags\":[\"AutoLearned\"],\"ingredients\":[{\"item_id\":19748,\"count\":3}],\"id\":8,\"chat_link\":\"[&CQgAAAA=]\"}]"

        when:
        def recipes = tradingPost.listRecipes([ 7319, 8 ])

        then:
        recipes.size() == 2
        recipes.get(0).id == 7319
        recipes.get(0).type == "RefinementEctoplasm"
        recipes.get(0).minRating == 450
        recipes.get(0).disciplines == ["Armorsmith","Artificer","Weaponsmith","Huntsman"]
        recipes.get(0).outputItemId == 46742
        recipes.get(0).ingredients.size() == 3
        recipes.get(0).ingredients.get(0).itemId == 19684
        recipes.get(0).ingredients.get(0).count == 50
        recipes.get(0).ingredients.get(1).itemId == 19721
        recipes.get(0).ingredients.get(1).count == 1
        recipes.get(0).ingredients.get(2).itemId == 46747
        recipes.get(0).ingredients.get(2).count == 10

        recipes.get(1).id == 8
        recipes.get(1).type == "Refinement"
        recipes.get(1).outputItemId == 19747
        recipes.get(1).minRating == 300
        recipes.get(1).disciplines == ["Leatherworker","Armorsmith","Tailor","Scribe"]
        recipes.get(1).ingredients.size() == 1
        recipes.get(1).ingredients.get(0).itemId == 19748
        recipes.get(1).ingredients.get(0).count == 3
    }
}
