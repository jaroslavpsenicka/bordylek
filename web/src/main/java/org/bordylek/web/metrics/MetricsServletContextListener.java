package org.bordylek.web.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.MetricSet;
import com.codahale.metrics.jvm.BufferPoolMetricSet;
import com.codahale.metrics.jvm.GarbageCollectorMetricSet;
import com.codahale.metrics.jvm.MemoryUsageGaugeSet;
import com.codahale.metrics.jvm.ThreadStatesGaugeSet;
import com.codahale.metrics.log4j.InstrumentedAppender;
import com.codahale.metrics.servlets.MetricsServlet;
import org.apache.log4j.LogManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;
import java.lang.management.ManagementFactory;
import java.util.Map;

public class MetricsServletContextListener extends MetricsServlet.ContextListener {

    @Autowired
    private MetricRegistry registry;

    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
            .getAutowireCapableBeanFactory().autowireBean(this);
        super.contextInitialized(sce);

        // EhCache Instrumentation
        // see ehcache.xml

        // JVM Instrumentation
        registerAll("gc", new GarbageCollectorMetricSet(), registry);
        registerAll("buffers", new BufferPoolMetricSet(ManagementFactory.getPlatformMBeanServer()), registry);
        registerAll("memory", new MemoryUsageGaugeSet(), registry);
        registerAll("threads", new ThreadStatesGaugeSet(), registry);

        // Log4J Instrumentation
        InstrumentedAppender appender = new InstrumentedAppender(registry);
        appender.activateOptions();
        LogManager.getRootLogger().addAppender(appender);
    }

    @Override
    protected MetricRegistry getMetricRegistry() {
        return registry;
    }

    private void registerAll(String prefix, MetricSet metricSet, MetricRegistry registry) {
        for (Map.Entry<String, Metric> entry : metricSet.getMetrics().entrySet()) {
            if (entry.getValue() instanceof MetricSet) {
                registerAll(prefix + "." + entry.getKey(), (MetricSet) entry.getValue(), registry);
            } else {
                registry.register(prefix + "." + entry.getKey(), entry.getValue());
            }
        }
    }
}
