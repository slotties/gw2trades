package gw2trades.server.frontend

import gw2trades.repository.api.ItemRepository
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.time.LocalDateTime

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class HistoryControllerSpec extends Specification {
    /*
    @Subject
    HistoryController historyController
    ItemRepository itemRepository

    def setup() {
        itemRepository = Mock(ItemRepository)
        historyController = new HistoryController(itemRepository)
    }

    def list() {
        given:
        def fromDateStr = "1984-06-24T10:00:00"
        def expFromDate = LocalDateTime.parse(fromDateStr, HistoryController.DATE_FORMAT)
        def toDateStr = "2002-06-24T10:00:00"
        def expToDate = LocalDateTime.parse(toDateStr, HistoryController.DATE_FORMAT)

        when:
        historyController.list(123, fromDateStr, toDateStr)

        then:
        1 * itemRepository.getHistory(123, expFromDate, expToDate)
    }

    @Unroll("from=#from and to=#to")
    def listWithBadValues(String from, String to) {
        when:
        historyController.list(123, from, to)

        then:
        thrown IllegalArgumentException

        where:
        from                  | to
        null                  | "1984-06-24T10:00:00"
        "1984-06-24T10:00:00" | null
        "1984-06-24T10:00"    | "1984-06-24T10:00:00"
        "1984-06-24 10:00:00" | "1984-06-24T10:00:00"
    }
    */
}
