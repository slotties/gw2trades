package gw2trades.repository.filesystem

import com.fasterxml.jackson.databind.ObjectMapper
import gw2trades.repository.api.model.ItemListing
import gw2trades.repository.api.model.ItemListings
import gw2trades.repository.api.model.ListingStatistics
import org.apache.commons.io.FileUtils
import spock.lang.Specification
import spock.lang.Subject

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class FilesystemItemRepositorySpec extends Specification {
    @Subject
    FilesystemItemRepository repository

    private File tempDir;

    def setup() {
        this.tempDir = new File(System.getProperty("java.io.tmpdir"), "test_" + System.currentTimeMillis())
        this.tempDir.mkdirs()

        this.repository = new FilesystemItemRepository(this.tempDir)
    }

    def cleanup() {
        FileUtils.deleteDirectory(this.tempDir)
    }

    def storeSingle() {
        given:
        def listings =
                new ItemListings(
                        itemId: 123,
                        sells: [
                                new ItemListing(unitPrice: 5),
                                new ItemListing(unitPrice: 7),
                                new ItemListing(unitPrice: 1)
                        ],
                        buys: [
                                new ItemListing(unitPrice: 10),
                                new ItemListing(unitPrice: 17),
                                new ItemListing(unitPrice: 15)
                        ]
                )

        def timestamp = System.currentTimeMillis()
        def mapper = new ObjectMapper()

        when:
        this.repository.store([ listings ], timestamp)

        then:
        def dumpFile = new File(this.tempDir, "dumps/123_" + timestamp)
        assert dumpFile.exists()
        def dumpedListings = mapper.readValue(dumpFile, ItemListings.class)
        assert dumpedListings.itemId == listings.itemId
        assert dumpedListings.sells.size() == listings.sells.size()
        assert dumpedListings.sells.containsAll(listings.sells)
        assert dumpedListings.buys.size() == listings.buys.size()
        assert dumpedListings.buys.containsAll(listings.buys)

        // TODO: test when implemented
        // assert new File(this.tempDir, "history/123" + timestamp).exists()

        def statsFile = new File(this.tempDir, "stats.index")
        assert statsFile.exists()
        Map<Integer, ListingStatistics> stats = mapper.readValue(statsFile, mapper.getTypeFactory().constructMapType(Map.class, Integer.class, ListingStatistics.class));
        assert stats.size() == 1
        assert stats.containsKey(123)

        ListingStatistics listingStatistics = stats.get(123)
        assert listingStatistics != null
        assert listingStatistics.itemId == 123
        assert listingStatistics.buyStatistics != null
        assert listingStatistics.buyStatistics.minPrice == 10
        assert listingStatistics.buyStatistics.maxPrice == 17
        assert listingStatistics.sellStatistics != null
        assert listingStatistics.sellStatistics.minPrice == 1
        assert listingStatistics.sellStatistics.maxPrice == 7
    }
}
