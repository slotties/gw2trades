package gw2trades.server.model

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class PriceSpec extends Specification {
    @Subject
    Price price

    @Unroll("Testing valueOf(#coins)")
    def valueOf(int coins, Price expectedPrice) {
        when:
        price = Price.valueOf(coins)

        then:
        assert price.copperCoins == expectedPrice.copperCoins
        assert price.silverCoins == expectedPrice.silverCoins
        assert price.goldCoins == expectedPrice.goldCoins

        where:
        coins | expectedPrice
        7     | new Price(0, 0, 7)
        1337  | new Price(0, 13, 37)
        31337 | new Price(3, 13, 37)
    }
}
