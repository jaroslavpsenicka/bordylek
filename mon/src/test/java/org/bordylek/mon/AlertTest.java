package org.bordylek.mon;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.bordylek.mon.model.Alert;
import org.bordylek.mon.model.Severity;
import org.bordylek.mon.repository.AlertRepository;
import org.bordylek.service.model.Counter;
import org.bordylek.service.repository.MetricsRepository;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/mon-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml"})
public class AlertTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private AlertProcessor alertProcessor;

    @Autowired
    private ConfigurationService config;

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
        alertRepository.save(new Alert("rules.Name", new Date(0), Severity.INFO, "Hello"));
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
        Alert alert = new Alert("rules.Name", new Date(0), Severity.INFO, "Old");
        alert.setResolved(true);
        alertRepository.save(alert);
        alertRepository.save(new Alert("rules.Name", new Date(1000), Severity.INFO, "Hello"));
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
        Alert alert = new Alert("rules.Name1", new Date(0), Severity.ERROR, "Old");
        alert.setTimestamp(new Date(0));
        alert.setResolved(true);
        alertRepository.save(alert);
        alertRepository.save(new Alert("rules.Name2", new Date(1000), Severity.INFO, "Hello"));
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

    @Test
    public void resolve() throws Exception {
        alertRepository.save(new Alert("rules.Name", new Date(0), Severity.INFO, "Hello"));
        MvcResult result = mockMvc.perform(get("/alerts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fqName", is("rules.Name")))
            .andExpect(jsonPath("$[0].severity", is("INFO")))
            .andExpect(jsonPath("$[0].message", is("Hello")))
            .andExpect(jsonPath("$[0].resolved", is(false)))
            .andReturn();
        JsonNode jsonNode = new ObjectMapper().readTree(result.getResponse().getContentAsByteArray());
        String id = jsonNode.get(0).get("id").textValue();
        mockMvc.perform(post("/alerts/" + id + "/resolve")).andExpect(status().isOk());
        mockMvc.perform(get("/alerts").param("all", String.valueOf(true)))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].fqName", is("rules.Name")))
            .andExpect(jsonPath("$[0].severity", is("INFO")))
            .andExpect(jsonPath("$[0].message", is("Hello")))
            .andExpect(jsonPath("$[0].resolved", is(true)));
        mockMvc.perform(get("/alerts"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    public void resolveWrongId() throws Exception {
        mockMvc.perform(post("/alerts/ILLEGAL/resolve")).andExpect(status().isNotFound());
    }

    @Test
    public void doNotAlertDisabledRule() {
        Counter counter = new Counter();
        counter.setTimestamp(new Date(0));
        counter.setName("very-old");
        metricsRepository.save(counter);

        config.disableRule("rules.Age");
        alertProcessor.process();
        assertEquals(0, alertRepository.findAll().size());

        config.enableRule("rules.Age");
        alertProcessor.process();
        assertEquals(1, alertRepository.findAll().size());
        Alert alert = alertRepository.findAll().iterator().next();
        assertEquals("rules.Age", alert.getFqName());
        assertEquals("too old", alert.getMessage());
    }

}
