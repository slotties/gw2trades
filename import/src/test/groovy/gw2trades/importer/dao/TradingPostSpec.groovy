package gw2trades.importer.dao

import com.fasterxml.jackson.databind.ObjectMapper
import gw2trades.importer.http.ApiClient
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
        apiClient.get(_) >> "[1,5,100,31337]"

        when:
        def itemIds = tradingPost.listItemIds()

        then:
        itemIds == [ 1, 5, 100, 31337 ]
    }
}
