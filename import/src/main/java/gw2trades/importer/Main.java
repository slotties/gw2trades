package gw2trades.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.dao.TradingPost;
import gw2trades.importer.http.ApiClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * The importer pulls data from the Guild Wars 2 API and writes it into the repository used by the server module.
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Main {
    private static final Logger LOGGER = LogManager.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        ApiClient apiClient = new ApiClient();
        ObjectMapper objectMapper = new ObjectMapper();

        TradingPost tradingPost = new TradingPost();
        tradingPost.setApiClient(apiClient);
        tradingPost.setObjectMapper(objectMapper);

        Config config = readConfig("/importer.properties");

        Importer importer = new Importer(config, tradingPost);
        try {
            importer.execute();
        } catch (Exception e) {
            LOGGER.error("Could not import: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static Config readConfig(String fileName) throws IOException {
        Properties configData = new Properties();
        try (InputStream in = Main.class.getResourceAsStream(fileName)) {
            configData.load(in);

            return new Config(configData);
        }
    }
}
