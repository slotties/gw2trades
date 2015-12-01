package gw2trades.importer;

import java.util.Optional;
import java.util.Properties;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public final class Config {
    private final Properties config;

    public Config(Properties config) {
        this.config = config;
    }

    public String required(String key) throws IllegalArgumentException {
        String value = System.getProperty(key, config.getProperty(key));
        if (value == null) {
            throw new IllegalArgumentException();
        }

        return value;
    }

    public Optional<String> optional(String key) {
        try {
            String value = required(key);
            return Optional.of(value);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
