package gw2trades.repository.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbConnectionManager {
    private final String url;
    private final String userName;
    private final String password;

    public InfluxDbConnectionManager(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    public InfluxDB getConnection() {
        return InfluxDBFactory.connect(this.url, this.userName, this.password);
    }
}
