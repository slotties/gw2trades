package gw2trades.repository.filesystem

import com.fasterxml.jackson.databind.ObjectMapper
import gw2trades.repository.api.model.ItemListing
import gw2trades.repository.api.model.ItemListings
import gw2trades.repository.api.model.ListingStatistics
import gw2trades.repository.api.model.PriceStatistics
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
                                new ItemListing(unitPrice: 5, quantity: 1),
                                new ItemListing(unitPrice: 7, quantity: 1),
                                new ItemListing(unitPrice: 1, quantity: 1)
                        ],
                        buys: [
                                new ItemListing(unitPrice: 10, quantity: 1),
                                new ItemListing(unitPrice: 17, quantity: 1),
                                new ItemListing(unitPrice: 15, quantity: 1)
                        ]
                )

        def timestamp = System.currentTimeMillis()
        def mapper = new ObjectMapper()

        when:
        this.repository.store([listings], timestamp)

        then:
        def dumpFile = new File(this.tempDir, "dumps/123_" + timestamp)
        assert dumpFile.exists()
        def dumpedListings = mapper.readValue(dumpFile, ItemListings.class)
        assert dumpedListings.itemId == listings.itemId
        assert dumpedListings.sells.size() == listings.sells.size()
        assert dumpedListings.sells.containsAll(listings.sells)
        assert dumpedListings.buys.size() == listings.buys.size()
        assert dumpedListings.buys.containsAll(listings.buys)

        def historyFile = new File(this.tempDir, "histories/123")
        assert historyFile.exists()
        def historyContent = FileUtils.readFileToString(historyFile)
        assert historyContent.indexOf(timestamp + ":") == 0
        def json = historyContent.substring((timestamp + ":").length())
        def historyStats = mapper.readValue(json, ListingStatistics)
        assert historyStats != null
        assert historyStats.itemId == 123
        assert historyStats.buyStatistics != null
        assert historyStats.buyStatistics.minPrice == 10
        assert historyStats.buyStatistics.maxPrice == 17
        assert Math.floor(historyStats.buyStatistics.average) == Math.floor((10.0 + 17.0 + 15.0) / 3.0)
        assert historyStats.buyStatistics.totalAmount == 3
        // FIXME: assert listingStatistics.buyStatistics.median == 15
        assert historyStats.sellStatistics != null
        assert historyStats.sellStatistics.minPrice == 1
        assert historyStats.sellStatistics.maxPrice == 7
        assert Math.floor(historyStats.sellStatistics.average) == Math.floor((1.0 + 7.0 + 5.0) / 3.0)
        assert historyStats.sellStatistics.totalAmount == 3

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
        assert Math.floor(listingStatistics.buyStatistics.average) == Math.floor((10.0 + 17.0 + 15.0) / 3.0)
        assert listingStatistics.buyStatistics.totalAmount == 3
        // FIXME: assert listingStatistics.buyStatistics.median == 15
        assert listingStatistics.sellStatistics != null
        assert listingStatistics.sellStatistics.minPrice == 1
        assert listingStatistics.sellStatistics.maxPrice == 7
        assert Math.floor(listingStatistics.sellStatistics.average) == Math.floor((1.0 + 7.0 + 5.0) / 3.0)
        assert listingStatistics.sellStatistics.totalAmount == 3
        // FIXME: assert listingStatistics.sellStatistics.median == 5
    }

    def historyFile() {
        given:
        def listings1 = new ItemListings(
                itemId: 123,
                buys: [new ItemListing(unitPrice: 1, quantity: 1)],
                sells: [new ItemListing(unitPrice: 10, quantity: 10)]
        )
        def listings2 = new ItemListings(
                itemId: 123,
                buys: [new ItemListing(unitPrice: 2, quantity: 2)],
                sells: [new ItemListing(unitPrice: 20, quantity: 20)]
        )
        def listings3 = new ItemListings(
                itemId: 123,
                buys: [new ItemListing(unitPrice: 3, quantity: 3)],
                sells: [new ItemListing(unitPrice: 30, quantity: 30)]
        )

        def timestamp1 = System.currentTimeMillis()
        def timestamp2 = timestamp1 + 31337
        def timestamp3 = timestamp2 + 80085
        def mapper = new ObjectMapper()

        when:
        this.repository.store([listings1], timestamp1)
        this.repository.store([listings2], timestamp2)
        this.repository.store([listings3], timestamp3)

        then:
        def historyFile = new File(this.tempDir, "histories/123")
        assert historyFile.exists()
        def historyContent = FileUtils.readFileToString(historyFile)
        def lines = historyContent.split("\n")
        assert lines.length == 3

        // Timestamp 1
        assert lines[0].indexOf(timestamp1 + ":") == 0
        def json = lines[0].substring((timestamp1 + ":").length())
        def historyStats = mapper.readValue(json, ListingStatistics)
        assert historyStats == new ListingStatistics(
                itemId: 123,
                buyStatistics: new PriceStatistics(
                        minPrice: 1,
                        maxPrice: 1,
                        average: 1.0,
                        totalAmount: 1
                ),
                sellStatistics: new PriceStatistics(
                        minPrice: 10,
                        maxPrice: 10,
                        average: 10.0,
                        totalAmount: 10
                )
        )

        // Timestamp 2
        assert lines[1].indexOf(timestamp2 + ":") == 0
        def json2 = lines[1].substring((timestamp2 + ":").length())
        def historyStats2 = mapper.readValue(json2, ListingStatistics)
        assert historyStats2 == new ListingStatistics(
                itemId: 123,
                buyStatistics: new PriceStatistics(
                        minPrice: 2,
                        maxPrice: 2,
                        average: 2.0,
                        totalAmount: 2
                ),
                sellStatistics: new PriceStatistics(
                        minPrice: 20,
                        maxPrice: 20,
                        average: 20.0,
                        totalAmount: 20
                )
        )

        // Timestamp 3
        assert lines[2].indexOf(timestamp3 + ":") == 0
        def json3 = lines[2].substring((timestamp3 + ":").length())
        def historyStats3 = mapper.readValue(json3, ListingStatistics)
        assert historyStats3 == new ListingStatistics(
                itemId: 123,
                buyStatistics: new PriceStatistics(
                        minPrice: 3,
                        maxPrice: 3,
                        average: 3.0,
                        totalAmount: 3
                ),
                sellStatistics: new PriceStatistics(
                        minPrice: 30,
                        maxPrice: 30,
                        average: 30.0,
                        totalAmount: 30
                )
        )
    }
}
