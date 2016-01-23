package gw2trades.server;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.api.RecipeRepository;
import gw2trades.repository.influxdb.InfluxDbConnectionManagerImpl;
import gw2trades.repository.influxdb.InfluxDbRepository;
import gw2trades.repository.lucene.LuceneRecipeRepository;
import gw2trades.server.locale.LocaleInterceptor;
import gw2trades.server.locale.PerRequestLocaleResolver;
import gw2trades.server.security.RemoteAddrFilter;
import gw2trades.server.security.SecurityHeadersInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.velocity.VelocityProperties;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.ErrorPage;
import org.springframework.boot.context.embedded.FilterRegistrationBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.view.velocity.EmbeddedVelocityViewResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.LocaleResolver;
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
        InfluxDbConnectionManagerImpl connectionManager = new InfluxDbConnectionManagerImpl(
                this.environment.getProperty("influx.url"),
                this.environment.getProperty("influx.user"),
                this.environment.getProperty("influx.pass")
        );

        String indexDir = this.environment.getProperty("index.dir.items");
        LOGGER.info("Using {} as item index directory.", indexDir);

        return new InfluxDbRepository(connectionManager, indexDir, true);
    }

    @Bean
    public RecipeRepository recipeRepository() throws IOException {
        String indexDir = this.environment.getProperty("index.dir.recipes");
        LOGGER.info("Using {} as recipe index directory.", indexDir);

        return new LuceneRecipeRepository(indexDir, true);
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
        registry.addInterceptor(new LocaleInterceptor());
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new PerRequestLocaleResolver();
    }

    @Bean
    public EmbeddedServletContainerCustomizer containerCustomizer() {
        return (container -> {
            ErrorPage error404Page = new ErrorPage(HttpStatus.NOT_FOUND, "/404.html");
            ErrorPage error500Page = new ErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/500.html");

            container.addErrorPages(error404Page, error500Page);
        });
    }
}
