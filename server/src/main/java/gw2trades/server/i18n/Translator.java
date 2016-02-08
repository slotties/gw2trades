package gw2trades.server.i18n;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Translator {
    private final ResourceBundle resourceBundle;

    public Translator(ResourceBundle resourceBundle) {
        this.resourceBundle = resourceBundle;
    }

    public String message(String key) {
        return resourceBundle.getString(key);
    }

    public String message(String key, Object[] args) {
        String text = this.message(key);

        MessageFormat format = new MessageFormat(text);
        return format.format(args);
    }
}
