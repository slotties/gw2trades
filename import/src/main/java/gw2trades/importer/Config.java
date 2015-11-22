package gw2trades.importer;

import java.util.Map;
import java.util.Optional;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public final class Config {
    private final Map<String, ?> config;

    public Config(Map<String, ?> config) {
        this.config = config;
    }

    public String required(String... key) throws IllegalArgumentException {
        Map<String, ?> subConfig = this.config;

        for (int i = 0; i < key.length; i++) {
            Object valueObj = subConfig.get(key[i]);
            if (i == key.length - 1) {
                // We got to the leaf, so we expect a String.
                if (valueObj != null && !(valueObj instanceof Map)) {
                    return valueObj.toString();
                } else {
                    throw new IllegalArgumentException();
                }
            } else {
                // We got another sub config and expect a Map.
                if (valueObj instanceof Map) {
                    subConfig = (Map) valueObj;
                } else {
                    throw new IllegalArgumentException();
                }
            }
        }

        throw new IllegalArgumentException();
    }

    public Optional<String> optional(String... key) {
        try {
            String value = required(key);
            return Optional.of(value);
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }
}
