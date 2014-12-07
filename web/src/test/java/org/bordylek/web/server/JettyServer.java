package org.bordylek.web.server;

import java.io.IOException;
import java.util.EnumSet;

import javax.servlet.DispatcherType;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.DelegatingFilterProxy;
import org.springframework.web.servlet.DispatcherServlet;

public class JettyServer {

	private Server server;
	private WebApplicationContext applicationContext;

	private static final String CTX_PATH = "/bordylek";
	private static final String DISPATCHER_MAPPING = "/service/*";

	public void setApplicationContext(WebApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

    public ServerConnector start() throws Exception {
        server = new Server();
        server.setHandler(createServletContextHandler());
        ServerConnector connector = new ServerConnector(server);
        server.setConnectors(new Connector[] {connector});
        server.start();
        connector.setHost("localhost");
        return connector;
    }

    public void stop() throws Exception {
    	server.stop();
    }
    
    private ServletContextHandler createServletContextHandler() throws IOException {
        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath(CTX_PATH);
        context.addEventListener(new RequestContextListener());
        context.setClassLoader(Thread.currentThread().getContextClassLoader());
        
        FilterHolder securityFilter = new FilterHolder(
    		new DelegatingFilterProxy("springSecurityFilterChain", applicationContext));
        context.addFilter(securityFilter, DISPATCHER_MAPPING, EnumSet.allOf(DispatcherType.class));
    	
        ServletHolder dispatcherServlet = new ServletHolder("dispatcher", 
    		new DispatcherServlet(applicationContext));
        context.addServlet(dispatcherServlet, DISPATCHER_MAPPING);
        
        return context;
    }

}