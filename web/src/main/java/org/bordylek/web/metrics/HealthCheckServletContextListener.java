package org.bordylek.web.metrics;

import com.codahale.metrics.health.HealthCheck;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.servlets.HealthCheckServlet;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.BasicQuery;
import org.springframework.web.context.support.WebApplicationContextUtils;

import javax.servlet.ServletContextEvent;

public class HealthCheckServletContextListener extends HealthCheckServlet.ContextListener {

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Autowired
    private MongoTemplate mongoTemplate;

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckServletContextListener.class);

    public void contextInitialized(ServletContextEvent sce) {
        WebApplicationContextUtils.getRequiredWebApplicationContext(sce.getServletContext())
            .getAutowireCapableBeanFactory().autowireBean(this);
        super.contextInitialized(sce);

        healthCheckRegistry.register("mongo", new MongoHealthCheck(mongoTemplate));
    }

    @Override
    protected HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    public static class MongoHealthCheck extends HealthCheck {

        private MongoTemplate mongoTemplate;

        public MongoHealthCheck(MongoTemplate mongoTemplate) {
            this.mongoTemplate = mongoTemplate;
        }

        protected Result check() throws Exception {
            LOG.info("Running health check");
            if (mongoTemplate.findOne(new BasicQuery("{}"), User.class) == null)
                throw new IllegalStateException("No users found");
            if (mongoTemplate.findOne(new BasicQuery("{}"), Community.class) == null)
                throw new IllegalStateException("No communities found");
            return Result.healthy();
        }
    }
}
