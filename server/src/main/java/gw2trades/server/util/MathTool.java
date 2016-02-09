package gw2trades.server.util;

/**
 * Just some helper methods for velocity because that damn template language cannot even do basic math operations.
 *
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class MathTool {
    public int floor(Object value) {
        if (value instanceof Number) {
            return (int) Math.floor(((Number) value).doubleValue());
        } else {
            return 0;
        }
    }

    public int add(int a, int b) {
        return a + b;
    }

    public int sub(int a, int b) {
        return a - b;
    }

    public int abs(int v) {
        return Math.abs(v);
    }
}
