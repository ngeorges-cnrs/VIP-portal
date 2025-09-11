package fr.insalyon.creatis.vip.core.server;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
@ComponentScan(
        // scan all WebMvc controller beans, except those in vip-api
        basePackages = "fr.insalyon.creatis.vip",
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.REGEX, pattern = "fr\\.insalyon\\.creatis\\.vip\\.api\\..*"),
        },
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = RestController.class),
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, value = WebMvcConfigurer.class),
        }
)
public class SpringInternalApiConfig {
    /* this class should remain empty, and only be used as the context configuration class
     * for the /internal API servlet and related beans.
     */
}
