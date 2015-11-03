package gw2trades.importer.http;

import java.io.*;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * The api client encapsulates common code to access the Guild Wars 2 API.
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class ApiClient {
    /**
     * Returns the response body of a given URL.
     * @param urlStr the URL to call
     * @return the response body
     * @throws IOException in case the API is not accessible
     */
    public String get(String urlStr) throws IOException {
        // TODO: implement unmarshalling
        // TODO: respect response code?
        URL url = new URL(urlStr);
        String str;

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"), 8 * 1024)) {
            str = reader.lines().collect(Collectors.joining());
        }

        return str.toString();
    }
}
