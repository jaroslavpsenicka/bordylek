package org.bordylek.web;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.ContextLoaderListener;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.context.support.XmlWebApplicationContext;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

import static org.junit.Assert.assertFalse;

public class EmbeddedHttpServer {

    private Server httpServer;

    public void start(int port) throws Exception {

        HandlerList handlers = new HandlerList();
        ResourceHandler resourceHandler = new ResourceHandler();
        resourceHandler.setDirectoriesListed(true);
        resourceHandler.setResourceBase("web/src/main/webapp");
        resourceHandler.setWelcomeFiles(new String[]{"index.html"});
        handlers.addHandler(resourceHandler);

        XmlWebApplicationContext ctx = new XmlWebApplicationContext();
        ctx.setConfigLocation("classpath:/dispatcher-servlet.xml");

        ServletHolder dispatcherServlet = new ServletHolder(new DispatcherServlet(ctx));
        dispatcherServlet.setInitOrder(1);
        ServletContextHandler springHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        springHandler.setContextPath("/*");
        springHandler.addServlet(dispatcherServlet, "/rest/*");
        springHandler.addEventListener(new ContextLoaderListener());
        springHandler.addEventListener(new RequestContextListener());
        springHandler.setInitParameter("contextConfigLocation", "classpath:/dispatcher-servlet.xml");

        FilterHolder springSecurityFilterChain = new FilterHolder(new DelegatingFilterProxy("springSecurityFilterChain"));
        springHandler.addFilter(springSecurityFilterChain, "/*", EnumSet.allOf(DispatcherType.class));
        handlers.addHandler(springHandler);

        this.httpServer = new Server(port);
        this.httpServer.setHandler(handlers);
        this.httpServer.start();
    }

    public void stop() throws Exception {
        httpServer.stop();
        int timeout = 10;
        while (timeout-- > 0 && httpServer.isRunning()) {
            Thread.sleep(500);
        }

        assertFalse(httpServer.isRunning());
    }

}
