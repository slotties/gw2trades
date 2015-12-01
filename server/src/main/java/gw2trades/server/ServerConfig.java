package gw2trades.server;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.influxdb.InfluxDbConnectionManager;
import gw2trades.repository.influxdb.InfluxDbRepository;
import gw2trades.server.security.RemoteAddrFilter;
import gw2trades.server.security.SecurityHeadersInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.velocity.VelocityProperties;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.view.velocity.EmbeddedVelocityViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Configuration
@ConfigurationProperties
public class ServerConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private VelocityProperties properties;

    @Bean
    public ItemRepository itemRepository() throws IOException {
        InfluxDbConnectionManager connectionManager = new InfluxDbConnectionManager(
                this.environment.getProperty("influx.url"),
                this.environment.getProperty("influx.user"),
                this.environment.getProperty("influx.pass")
        );

        String indexDir = this.environment.getProperty("index.dir");
        LOGGER.info("Using {} as index directory.", indexDir);

        return new InfluxDbRepository(connectionManager, indexDir, true);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        if (environment.getProperty("resources.disableCaching", Boolean.class, false)) {
            initDevelopmentResourceHandler(registry);
        } else {
            initProductionResourceHandler(registry);
        }
    }

    private void initProductionResourceHandler(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**").addResourceLocations("classpath:/static/");
    }

    private void initDevelopmentResourceHandler(ResourceHandlerRegistry registry) {
        try {
            String localFilesystem = new File("server/src/main/resources/static").toURI().toURL().toString();

            registry.addResourceHandler("/static/**")
                    .addResourceLocations(localFilesystem)
                    .setCachePeriod(0)
                    .resourceChain(false);
        } catch (MalformedURLException e) {
            LOGGER.error("Could not setup resource handler for local filesystem. Using the original one.", e);
        }
    }

    @Bean
    public ViewResolver viewResolver() {
        EmbeddedVelocityViewResolver resolver = new EmbeddedVelocityViewResolver();
        this.properties.applyToViewResolver(resolver);
        return resolver;
    }

    @Bean
    public FilterRegistrationBean localHostOnlyFilter() {
        RemoteAddrFilter filter = new RemoteAddrFilter();
        filter.setAllow("127\\.\\d+\\.\\d+\\.\\d+|::1|0:0:0:0:0:0:0:1");

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.addUrlPatterns(
                "/env",
                "/metrics",
                "/dump",
                "/configprops",
                "/mappings",
                "/autoconfig",
                "/health",
                "/trace",
                "/beans",
                "/info",
                "/admin/**"
        );

        return registration;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SecurityHeadersInterceptor());
    }
}
