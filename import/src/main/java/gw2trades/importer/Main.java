package gw2trades.importer;

import com.fasterxml.jackson.databind.ObjectMapper;
import gw2trades.importer.dao.TradingPost;
import gw2trades.importer.http.ApiClient;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * TODO
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Main {
    public static void main(String[] args) throws Exception {
        ApiClient apiClient = new ApiClient();
        ObjectMapper objectMapper = new ObjectMapper();

        TradingPost tradingPost = new TradingPost();
        tradingPost.setApiClient(apiClient);
        tradingPost.setObjectMapper(objectMapper);

        Config config = readConfig("/import.yaml");

        Importer importer = new Importer(config, tradingPost);
        // TODO: handle errors
        importer.execute();
    }

    private static Config readConfig(String fileName) throws IOException {
        Yaml yamlConfig = new Yaml();
        try (InputStream in = Main.class.getResourceAsStream(fileName)) {
            Map configData = (Map) yamlConfig.load(in);
            return new Config(configData);
        }
    }
}
