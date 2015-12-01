package gw2trades.server.model;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class Price {
    private final int goldCoins;
    private final int silverCoins;
    private final int copperCoins;

    public Price(int goldCoins, int silverCoins, int copperCoins) {
        this.goldCoins = goldCoins;
        this.silverCoins = silverCoins;
        this.copperCoins = copperCoins;
    }

    public static Price valueOf(int copperCoins) {
        int copper = copperCoins % 100;
        copperCoins = (int) Math.floor((float) copperCoins / 100.0);

        int silver = copperCoins % 100;
        copperCoins = (int) Math.floor((float) copperCoins / 100.0);

        int gold = copperCoins;

        return new Price(gold, silver, copper);
    }

    public int getCopperCoins() {
        return copperCoins;
    }

    public int getGoldCoins() {
        return goldCoins;
    }

    public int getSilverCoins() {
        return silverCoins;
    }
}
