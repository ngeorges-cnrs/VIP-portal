package fr.insalyon.creatis.vip.api;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.bind.annotation.RestController;

@Configuration
@EnableWebMvc
@ComponentScan(includeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, value=RestController.class))
public class Api1Config {
}