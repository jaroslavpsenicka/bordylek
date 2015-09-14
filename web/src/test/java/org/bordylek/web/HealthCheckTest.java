package org.bordylek.web;

import com.codahale.metrics.health.HealthCheckRegistry;
import org.bordylek.web.metrics.HealthCheckServletContextListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.CoreMatchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/web-context.xml",
    "/security-context.xml", "/social-context.xml", "/metrics-context.xml", "/test-context.xml"})
public class HealthCheckTest {

    @Autowired
	private WebApplicationContext webApplicationContext;

    @Autowired
    private HealthCheckRegistry healthCheckRegistry;

    @Autowired
    private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @Before
	public void before() throws Exception {
        healthCheckRegistry.register("mongo", new HealthCheckServletContextListener.MongoHealthCheck(mongoTemplate));
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@After
	public void after() throws Exception {
    }

    @Test
    public void check() throws Exception {
        mockMvc.perform(get("/health/check"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("mongo.healthy", is(false)))
            .andExpect(jsonPath("mongo.message", is("No users found")));
    }

}
