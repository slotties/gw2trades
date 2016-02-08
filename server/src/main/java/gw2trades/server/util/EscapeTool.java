package gw2trades.server.util;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class EscapeTool {
    public String html(String text) {
        return StringEscapeUtils.escapeHtml(text);
    }
}
