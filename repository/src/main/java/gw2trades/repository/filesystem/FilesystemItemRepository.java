package gw2trades.repository.filesystem;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.model.ItemListing;
import gw2trades.repository.api.model.ItemListings;
import gw2trades.repository.api.model.ListingStatistics;
import gw2trades.repository.api.model.PriceStatistics;

import java.io.*;
import java.util.*;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class FilesystemItemRepository implements ItemRepository {
    private File directory;

    private File statisticsIndex;
    private File historyDirectory;
    private File dumpDirectory;

    private ObjectMapper objectMapper;

    public FilesystemItemRepository(File directory) {
        this.directory = directory;

        this.statisticsIndex = new File(this.directory, "stats.index");
        this.historyDirectory = new File(this.directory, "histories");
        this.dumpDirectory = new File(this.directory, "dumps");

        this.objectMapper = new ObjectMapper();
    }

    @Override
    public Collection<ListingStatistics> listStatistics() throws IOException {
        Map<Integer, ListingStatistics> stats = readStatistics();
        return stats.values();
    }

    @Override
    public List<ListingStatistics> getHistory(int itemId, long fromTimestamp, long toTimestamp) throws IOException {
        List<ListingStatistics> listings = new ArrayList<>();

        File historyFile = new File(this.historyDirectory, Integer.toString(itemId));
        try (BufferedReader reader = new BufferedReader(new FileReader(historyFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                int tsIdx = line.indexOf(':');
                long timestamp = Long.valueOf(line.substring(0, tsIdx));
                if (timestamp >= fromTimestamp && timestamp <= toTimestamp) {
                    ListingStatistics stats = this.objectMapper.readValue(line.substring(tsIdx + 1), ListingStatistics.class);
                    listings.add(stats);
                }
            }
        }

        return listings;
    }

    @Override
    public ListingStatistics latestStatistics(int itemId) throws IOException {
        Map<Integer, ListingStatistics> stats = readStatistics();
        // TODO: throw exception when unknown?
        return stats.get(itemId);
    }

    @Override
    public void store(Collection<ItemListings> listings, long timestamp) throws IOException {
        Map<Integer, ListingStatistics> statisticsIndex = readStatistics();

        for (ItemListings listing : listings) {
            ListingStatistics stats = createStatistics(listing);
            stats.setTimestamp(timestamp);

            appendHistory(stats, timestamp);
            writeFullListing(listing, timestamp);
            statisticsIndex.put(listing.getItemId(), stats);
        }

        writeStatistics(statisticsIndex);
    }

    private Map<Integer, ListingStatistics> readStatistics() throws IOException {
        Map<Integer, ListingStatistics> stats;
        if (this.statisticsIndex.exists()) {
            stats = this.objectMapper.readValue(this.statisticsIndex, objectMapper.getTypeFactory().constructMapType(Map.class, Integer.class, ListingStatistics.class));
        } else {
            stats = new HashMap<>();
        }

        return stats;
    }

    private ListingStatistics createStatistics(ItemListings listings) {
        ListingStatistics stats = new ListingStatistics();
        stats.setItemId(listings.getItemId());
        stats.setBuyStatistics(createPriceStatistics(listings.getBuys()));
        stats.setSellStatistics(createPriceStatistics(listings.getSells()));

        return stats;
    }

    private PriceStatistics createPriceStatistics(Collection<ItemListing> listings) {
        PriceStatistics stats = new PriceStatistics();

        int minPrice = Integer.MAX_VALUE;
        int maxPrice = 0;
        int totalAmount = 0;
        int totalPrice = 0;

        for (ItemListing listing : listings) {
            minPrice = Math.min(minPrice, listing.getUnitPrice());
            maxPrice = Math.max(maxPrice, listing.getUnitPrice());
            totalAmount += listing.getQuantity();
            totalPrice += (listing.getQuantity() * listing.getUnitPrice());
        }

        stats.setMaxPrice(maxPrice);
        stats.setMinPrice(minPrice);
        if (totalAmount != 0) {
            stats.setAverage((double) totalPrice / (double) totalAmount);
        }

        stats.setTotalAmount(totalAmount);

        return stats;
    }

    private void ensureDirectoryExists(File directory) throws IOException {
        if (!directory.exists()) {
            directory.mkdirs();
            if (!directory.exists()) {
                throw new IOException("Could not create '" + directory.getAbsolutePath() + "'");
            }
        }
    }

    private void appendHistory(ListingStatistics stats, long timestamp) throws IOException {
        ensureDirectoryExists(this.historyDirectory);

        File file = new File(this.historyDirectory, Integer.toString(stats.getItemId()));

        String jsonString = this.objectMapper.writeValueAsString(stats);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
            writer.write(Long.toString(timestamp));
            writer.write(":");
            writer.write(jsonString);
            writer.write("\n");
            writer.flush();
        }
    }

    private void writeFullListing(ItemListings listings, long timestamp) throws IOException {
        ensureDirectoryExists(this.dumpDirectory);

        File file = new File(this.dumpDirectory, listings.getItemId() + "_" + timestamp);
        this.objectMapper.writeValue(file, listings);
    }

    private void writeStatistics(Map<Integer, ListingStatistics> stats) throws IOException {
        ensureDirectoryExists(this.directory);

        File file = this.statisticsIndex;
        this.objectMapper.writeValue(file, stats);
    }
}
