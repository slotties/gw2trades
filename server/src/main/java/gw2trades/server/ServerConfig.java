package gw2trades.server;

import gw2trades.repository.api.ItemRepository;
import gw2trades.repository.filesystem.FilesystemItemRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.io.File;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
@Configuration
@ConfigurationProperties
public class ServerConfig extends WebMvcConfigurerAdapter {
    private static final Logger LOGGER = LogManager.getLogger(ServerConfig.class);

    @Autowired
    private Environment environment;

    @Bean
    public ItemRepository itemRepository() {
        File directory = new File(this.environment.getProperty("repo.fs.dir"));
        LOGGER.info("Using {} as filesystem repository directory.", directory.getAbsolutePath());

        return new FilesystemItemRepository(directory);
    }
}
