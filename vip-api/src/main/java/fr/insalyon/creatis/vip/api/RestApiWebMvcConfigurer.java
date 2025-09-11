package fr.insalyon.creatis.vip.api;

import fr.insalyon.creatis.vip.api.business.VipConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;

import java.util.Collections;

import fr.insalyon.creatis.vip.core.server.business.Server;

@Configuration
public class RestApiWebMvcConfigurer implements WebMvcConfigurer {

    private final Server server;
    private final VipConfigurer vipConfigurer;

    @Autowired
    public RestApiWebMvcConfigurer(Server server, VipConfigurer vipConfigurer) {
        this.server = server;
        this.vipConfigurer = vipConfigurer;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void configurePathMatch(PathMatchConfigurer configurer) {
        // this is deprecated, but temporary necessary for shanoir
        // that uses requests like /rest/pipelines/
        // Shanoir should get rid of the trailing slash and we should
        // be able to remove this method when spring removes its support
        configurer.setUseTrailingSlashMatch(true);
    }

    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // necessary in the content negotiation stuff of carmin data
        // this should be the default in Spring 5.3 and may be removed then
        configurer.useRegisteredExtensionsOnly(true);
        configurer.replaceMediaTypes(Collections.emptyMap());
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "HEAD")
                .allowedOrigins(server.getCarminCorsAuthorizedDomains());
    }

    /*
     to verify that the proxy is still valid each day
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(vipConfigurer);
    }
}
