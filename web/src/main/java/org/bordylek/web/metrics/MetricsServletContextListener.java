package org.bordylek.web.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.servlets.MetricsServlet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Autowired
    private MetricRegistry metricRegistry;

    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
            .getAutowireCapableBeanFactory().autowireBean(this);
        super.contextInitialized(sce);
    }

    @Override
    protected MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

}
