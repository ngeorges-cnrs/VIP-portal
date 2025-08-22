package fr.insalyon.creatis.vip.api;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebAppInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // return null; // or specify root context config classes
        return new Class<?>[] { org.springframework.web.context.support.AnnotationConfigWebApplicationContext.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class<?>[] { fr.insalyon.creatis.vip.core.server.SpringCoreConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/rest/*" };
    }
}
