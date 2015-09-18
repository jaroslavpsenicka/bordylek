package org.bordylek.mon;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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
@SpringApplicationConfiguration(classes = MonApplication.class, locations = {"classpath:/test-context.xml"})
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
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.Basic", hasSize(2)))
            .andExpect(jsonPath("data.Basic[0].name", is("Name")))
            .andExpect(jsonPath("data.Basic[0].enabled", is(true)))
            .andExpect(jsonPath("data.Basic[1].name", is("Age")))
            .andExpect(jsonPath("data.Basic[1].enabled", is(true)));
    }

    @Test
    public void disableRule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/rules/toggle")
            .header("Content-Type", "application/json").content("Basic.Name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("enabled", is(false)));
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.Basic", hasSize(2)))
            .andExpect(jsonPath("data.Basic[0].name", is("Name")))
            .andExpect(jsonPath("data.Basic[0].enabled", is(false)));
    }

    @Test
    public void enableRule() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.post("/rest/rules/toggle")
            .header("Content-Type", "application/json").content("Basic.Name"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("enabled", is(true)));
        mockMvc.perform(MockMvcRequestBuilders.get("/rest/rules"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.Basic", hasSize(2)))
            .andExpect(jsonPath("data.Basic[0].name", is("Name")))
            .andExpect(jsonPath("data.Basic[0].enabled", is(true)));
    }

}
