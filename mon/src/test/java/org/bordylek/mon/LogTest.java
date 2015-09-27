package org.bordylek.mon;

import org.bordylek.service.model.Log;
import org.bordylek.service.repository.LogRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/application-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml", "/integration-context.xml"})
public class LogTest {

    @Autowired
    private LogRepository logRepository;

    @Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;

    private MockMvc mockMvc;

	@Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "monitor");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@After
	public void after() throws Exception {
    }

    @Test
    public void get() throws Exception {
        Log log = logRepository.save(new Log("Log1", new Date(0)));
        mockMvc.perform(MockMvcRequestBuilders.get("/logs/" + log.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("message", is("Log1")));
    }

    @Test
    public void getUnknown() throws Exception {
        Log log = logRepository.save(new Log("Log1", new Date(0)));
        mockMvc.perform(MockMvcRequestBuilders.get("/logs/UNKNOWN"))
            .andExpect(status().isNotFound());
    }


}
