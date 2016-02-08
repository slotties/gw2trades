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
    public ViewResolver viewResolver() {
        EmbeddedVelocityViewResolver resolver = new EmbeddedVelocityViewResolver();
        this.properties.applyToViewResolver(resolver);
        return resolver;
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
