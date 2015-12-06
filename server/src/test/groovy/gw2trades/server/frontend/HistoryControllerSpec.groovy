package gw2trades.server.frontend

import gw2trades.repository.api.ItemRepository
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class HistoryControllerSpec extends Specification {
    @Subject
    HistoryController historyController
    ItemRepository itemRepository

    def setup() {
        itemRepository = Mock(ItemRepository)
        historyController = new HistoryController(itemRepository)
    }

    def list() {
        when:
        historyController.list(123, "1984-06-24T10:00:00", "2002-06-24T10:00:00")

        then:
        1 * itemRepository.getHistory(123, 456912000000, 1024905600000l)
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
}
