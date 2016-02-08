package gw2trades.server;

/**
 * @author Stefan Lotties (slotties@gmail.com)
 */
// TODO
public class ServerConfig { /*extends WebMvcConfigurerAdapter {
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
    */
}
