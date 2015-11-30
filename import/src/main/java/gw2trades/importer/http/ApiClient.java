package gw2trades.importer.http;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * The api client encapsulates common code to access the Guild Wars 2 API.
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ApiClient {
    private static final Logger LOGGER = LogManager.getLogger(ApiClient.class);

    /**
     * Returns the response body of a given URL.
     * @param urlStr the URL to call
     * @return the response body
     * @throws IOException in case the API is not accessible
     */
    public String get(String urlStr) throws IOException {
        URL url = new URL(urlStr);
        String str;

        long t0 = System.currentTimeMillis();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"), 8 * 1024)) {
            str = reader.lines().collect(Collectors.joining());
        }

        if (LOGGER.isDebugEnabled()) {
            long t1 = System.currentTimeMillis();
            LOGGER.debug("Pulling from {} took {} ms.", urlStr, t1 - t0);
        }

        return str.toString();
    }
}
