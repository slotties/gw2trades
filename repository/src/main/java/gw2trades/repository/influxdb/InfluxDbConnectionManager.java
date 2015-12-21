package gw2trades.repository.influxdb;

import org.influxdb.InfluxDB;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
public interface InfluxDbConnectionManager {
    InfluxDB getConnection();
}
