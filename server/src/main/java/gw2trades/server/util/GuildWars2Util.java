package gw2trades.server.util;

import gw2trades.server.model.Price;
import org.apache.commons.lang.StringEscapeUtils;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class GuildWars2Util {
    public Price price(int coins) {
        return Price.valueOf(coins);
    }

    public int profit(int buyingPrice, int sellingPrice) {
        int fixCosts = (int) Math.floor(((float) sellingPrice) * 0.15f);
        int profit = (sellingPrice - buyingPrice) - fixCosts;

        return profit;
    }

    public String wikiName(String itemName) {
        if (itemName == null) {
            return null;
        }

        String escapedItemName = itemName.replace(' ', '_');
        escapedItemName = StringEscapeUtils.escapeHtml(escapedItemName);

        return escapedItemName;
    }
}
