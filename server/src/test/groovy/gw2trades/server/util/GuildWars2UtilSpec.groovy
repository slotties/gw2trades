package gw2trades.server.util

import gw2trades.server.model.Price
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class GuildWars2UtilSpec extends Specification {
    @Subject
    GuildWars2Util guildWars2Util = new GuildWars2Util()

    @Unroll("price conversion of #input coins")
    def price(int input, Price expectedPrice) {
        when:
        def price = guildWars2Util.price(input)

        then:
        assert price.copperCoins == expectedPrice.copperCoins
        assert price.silverCoins == expectedPrice.silverCoins
        assert price.goldCoins == expectedPrice.goldCoins

        where:
        input | expectedPrice
        7     | new Price(0, 0, 7)
        1337  | new Price(0, 13, 37)
        31337 | new Price(3, 13, 37)
    }

    @Unroll("profit of #buyingPrice and #sellingPrice")
    def profit(int buyingPrice, int sellingPrice, int expectedProfit) {
        when:
        def profit = guildWars2Util.profit(buyingPrice, sellingPrice)

        then:
        assert profit == expectedProfit

        where:
        buyingPrice | sellingPrice | expectedProfit
        31337       | 80085        | 36736
        100         | 10           | -91
        100         | 100          | -15
    }

    @Unroll("wikiName for #input")
    def wikiName(String input, String expectedOutput) {
        when:
        def output = guildWars2Util.wikiName(input)

        then:
        assert output == expectedOutput

        where:
        input       | expectedOutput
        null        | null
        "Test"      | "Test"
        "foo bar"   | "foo_bar"
        "foo's bar" | "foo's_bar"
    }
}
