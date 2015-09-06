package org.bordylek.web.metrics;

import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
            .getAutowireCapableBeanFactory().autowireBean(this);
        super.contextInitialized(sce);
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

}
