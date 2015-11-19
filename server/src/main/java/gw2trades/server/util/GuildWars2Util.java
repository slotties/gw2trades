package gw2trades.server.util;

import gw2trades.server.model.Price;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class GuildWars2Util {
    public Price price(int coins) {
        return Price.valueOf(coins);
    }

    public int profit(int buyingPrice, int sellingPrice) {
        int fixCosts = (int) Math.floor((float) sellingPrice * 0.15f);
        int profit = (sellingPrice - buyingPrice) - fixCosts;

        return profit;
    }
}
