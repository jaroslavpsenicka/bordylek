package org.bordylek.mon;

import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.bordylek.mon.repository.AlertRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/mon-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml"})
public class AlertTest {

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

    @Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "alerts");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
    }

    @Test
    public void activeAlertsOnly() throws Exception {
        alertRepository.save(new Alert("rules", "Name", Severity.INFO, "Hello"));
        mockMvc.perform(get("/alerts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fqName", is("rules.Name")))
            .andExpect(jsonPath("$[0].severity", is("INFO")))
            .andExpect(jsonPath("$[0].message", is("Hello")))
            .andExpect(jsonPath("$[0].resolved", is(false)));
    }

    @Test
    public void activeAlerts() throws Exception {
        Alert alert = new Alert("rules", "Name", Severity.INFO, "Old");
        alert.setResolved(true);
        alertRepository.save(alert);
        alertRepository.save(new Alert("rules", "Name", Severity.INFO, "Hello"));
        mockMvc.perform(get("/alerts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fqName", is("rules.Name")))
            .andExpect(jsonPath("$[0].severity", is("INFO")))
            .andExpect(jsonPath("$[0].message", is("Hello")))
            .andExpect(jsonPath("$[0].resolved", is(false)));
    }

    @Test
    public void allAlerts() throws Exception {
        Alert alert = new Alert("rules", "Name1", Severity.ERROR, "Old");
        alert.setTimestamp(new Date(0));
        alert.setResolved(true);
        alertRepository.save(alert);
        alertRepository.save(new Alert("rules", "Name2", Severity.INFO, "Hello"));
        mockMvc.perform(get("/alerts").param("all", "true"))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].fqName", is("rules.Name2")))
            .andExpect(jsonPath("$[0].severity", is("INFO")))
            .andExpect(jsonPath("$[0].message", is("Hello")))
            .andExpect(jsonPath("$[0].resolved", is(false)))
            .andExpect(jsonPath("$[1].fqName", is("rules.Name1")))
            .andExpect(jsonPath("$[1].severity", is("ERROR")))
            .andExpect(jsonPath("$[1].message", is("Old")))
            .andExpect(jsonPath("$[1].resolved", is(true)));
    }

}
