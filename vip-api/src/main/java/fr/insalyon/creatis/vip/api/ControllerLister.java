package fr.insalyon.creatis.vip.api;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.util.Map;

@Component
public class ControllerLister implements ApplicationListener<ContextRefreshedEvent> {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private ApplicationContext context;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        if (context instanceof WebApplicationContext) {
            // WebApplicationContext ctx = (WebApplicationContext)context;
            // ctx.getServletContext().getContextPath();
            Map<String, HandlerMapping> mappings = context.getBeansOfType(HandlerMapping.class);
            mappings.forEach((name, mapping) -> {
                if (mapping instanceof RequestMappingHandlerMapping) {
                    RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) mapping;
                    Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
                    handlerMethods.forEach((info, method) -> {
                        logger.info("Controller: " + method.getBeanType().getName() + ", Method: " + method.getMethod().getName() + ", Patterns: " + info.getPatternsCondition());
                    });
                }
            });
        }
    }
}