package gw2trades.server.frontend.sorters

import gw2trades.repository.api.model.ListingStatistics
import gw2trades.repository.api.model.PriceStatistics
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class BuyersByAveragePriceSpec extends Specification {
    @Subject
    Comparator<ListingStatistics> comparator = new BuyersByAveragePrice()

    ListingStatistics stats1 = new ListingStatistics(
            buyStatistics: new PriceStatistics(average: 1.0)
    )
    ListingStatistics stats2 = new ListingStatistics(
            buyStatistics: new PriceStatistics(average: 2.0)
    )

    def smallerThan() {
        when:
        int r = comparator.compare(stats1, stats2)

        then:
        assert r < 0
    }

    def biggerThan() {
        when:
        int r = comparator.compare(stats2, stats1)

        then:
        assert r > 0
    }

    def sameValue() {
        when:
        int r = comparator.compare(stats1, stats1)

        then:
        assert r == 0
    }
}
