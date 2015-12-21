package gw2trades.repository.influxdb;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDBFactory;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public class InfluxDbConnectionManagerImpl implements InfluxDbConnectionManager {
    private final String url;
    private final String userName;
    private final String password;

    public InfluxDbConnectionManagerImpl(String url, String userName, String password) {
        this.url = url;
        this.userName = userName;
        this.password = password;
    }

    @Override
    public InfluxDB getConnection() {
        return InfluxDBFactory.connect(this.url, this.userName, this.password);
    }
}
