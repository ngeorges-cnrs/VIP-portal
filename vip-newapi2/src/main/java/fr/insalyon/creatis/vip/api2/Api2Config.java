package fr.insalyon.creatis.vip.newapi2;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.RestController;

//import fr.insalyon.creatis.vip.core.server.business.BusinessException;

@Configuration
@EnableWebMvc
//@ComponentScan(basePackages = "fr.insalyon.creatis.vip.newapi2", includeFilters = @ComponentScan.Filter(RestController.class))
@ComponentScan(includeFilters = @ComponentScan.Filter(type=FilterType.ANNOTATION, value=RestController.class))
public class Api2Config {
}