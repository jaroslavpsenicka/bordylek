package org.bordylek.mon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/web-context.xml", "/security-context.xml", "/test-context.xml"})
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class RulesTest {

    @Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "metrics");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@After
	public void after() throws Exception {
    }

    @Test
    public void listRules() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.get("/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.['org.bordylek.mon.rules']", hasSize(2)))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].name", is("org.bordylek.mon.rules.Name")))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].enabled", is(true)))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][1].name", is("org.bordylek.mon.rules.Age")))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][1].enabled", is(true)));
    }

    @Test
    public void disableRule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/rules/toggle")
            .header("Content-Type", "application/json").content("org.bordylek.mon.rules.Name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("enabled", is(false)));
        mockMvc.perform(MockMvcRequestBuilders.get("/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.['org.bordylek.mon.rules']", hasSize(2)))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].name", is("org.bordylek.mon.rules.Name")))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].enabled", is(false)));
    }

    @Test
    public void enableRule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/rules/toggle")
            .header("Content-Type", "application/json").content("org.bordylek.mon.rules.Name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("enabled", is(false)));
        mockMvc.perform(MockMvcRequestBuilders.get("/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.['org.bordylek.mon.rules']", hasSize(2)))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].name", is("org.bordylek.mon.rules.Name")))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].enabled", is(false)));
        mockMvc.perform(MockMvcRequestBuilders.post("/rules/toggle")
            .header("Content-Type", "application/json").content("org.bordylek.mon.rules.Name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("enabled", is(true)));
        mockMvc.perform(MockMvcRequestBuilders.get("/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.['org.bordylek.mon.rules']", hasSize(2)))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].name", is("org.bordylek.mon.rules.Name")))
            .andExpect(jsonPath("$.['org.bordylek.mon.rules'][0].enabled", is(true)));
    }

}
