package gw2trades.server;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Configuration
@ConfigurationProperties
public class ServerConfig extends WebMvcConfigurerAdapter {
}
