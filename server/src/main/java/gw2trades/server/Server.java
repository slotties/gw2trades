package gw2trades.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@EnableAutoConfiguration
@ComponentScan
public class Server {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Server.class, args);
    }
}
