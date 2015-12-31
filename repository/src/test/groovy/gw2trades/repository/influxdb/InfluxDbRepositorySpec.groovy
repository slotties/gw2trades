package gw2trades.repository.influxdb

import gw2trades.repository.api.model.Item
import gw2trades.repository.api.model.ItemListing
import gw2trades.repository.api.model.ItemListings
import org.apache.commons.io.FileUtils
import org.apache.lucene.index.DirectoryReader
import org.apache.lucene.store.FSDirectory
import org.influxdb.InfluxDB
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.file.Paths

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
class InfluxDbRepositorySpec extends Specification {
    @Subject
    InfluxDbRepository repository

    InfluxDB influxDB
    InfluxDbConnectionManager connectionManager
    File tmpDir

    def setup() {
        influxDB = Mock(InfluxDB)
        connectionManager = Mock(InfluxDbConnectionManager)
        connectionManager.getConnection() >> influxDB

        tmpDir = new File(System.getProperty("java.io.tmpdir"), Long.toString(System.currentTimeMillis()))

        repository = new InfluxDbRepository(connectionManager, tmpDir.getAbsolutePath(), false)
    }

    def cleanup() {
        FileUtils.deleteDirectory(tmpDir)
    }

    @Unroll("profitScore of #sellPrice (sell) to #buyPrice (buy)")
    def profitScore(int buyPrice, int sellPrice, double minProfitScore, double maxProfitScore) throws Exception {
        given:
        def listings = new ItemListings(
                itemId: 123,
                buys: [ new ItemListing(unitPrice: buyPrice, quantity: 1) ],
                sells: [ new ItemListing(unitPrice: sellPrice, quantity: 1 )],
                item: new Item(
                        itemId: 123,
                        name: "foo",
                        iconUrl: "foo",
                        rarity: "rare",
                        type: "Armor"
                )
        )

        when:
        repository.store([ listings ], System.currentTimeMillis())
        repository.close()

        def indexReader = DirectoryReader.open(FSDirectory.open(Paths.get(tmpDir.getAbsolutePath())))
        def document = indexReader.document(indexReader.maxDoc() - 1)
        indexReader.close()

        then:
        def profitScore = Double.valueOf(document.get("profit_score"))
        profitScore >= minProfitScore
        profitScore <= maxProfitScore

        where:
        buyPrice | sellPrice | minProfitScore | maxProfitScore
        100      | 50        | 0.0            | 0.1
        100      | 100       | 0.0            | 0.1
        100      | 125       | 0.5            | 0.6
        100      | 150       | 0.5            | 0.6
        100      | 200       | 0.7            | 0.8
        100      | 250       | 0.8            | 0.9
        100      | 90        | 0.0            | 0.1
        1000     | 500       | 0.0            | 0.1
        1000     | 1000      | 0.0            | 0.1
        1000     | 1250      | 0.5            | 0.6
        1000     | 1500      | 0.5            | 0.6
        1000     | 1750      | 0.6            | 0.7
        1000     | 2000      | 0.7            | 0.8
        1000     | 10000     | 0.3            | 0.4
    }
}
