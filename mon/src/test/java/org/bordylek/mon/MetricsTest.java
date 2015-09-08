package org.bordylek.mon;

import org.bordylek.service.model.AbstractMetric;
import org.bordylek.service.model.Counter;
import org.bordylek.service.repository.MetricsRepository;
import org.junit.After;
import org.junit.Assert;
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
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/web-context.xml", "/security-context.xml", "/test-context.xml"})
public class MetricsTest {

    @Autowired
    private MetricsRepository metricsRepository;

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
    public void findTopMetric() {
        Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(0));
        counter1.setName("old");
        metricsRepository.save(counter1);
        Counter counter2 = new Counter();
        counter2.setTimestamp(new Date(500));
        counter2.setName("old");
        metricsRepository.save(counter2);
        Counter counter3 = new Counter();
        counter3.setTimestamp(new Date(1000));
        counter3.setName("new");
        metricsRepository.save(counter3);

        AbstractMetric metric = metricsRepository.findTopByOrderByTimestampDesc();
        Assert.assertEquals("new", metric.getName());
    }

    @Test
    public void latestMetrics() throws Exception {
        Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(0));
        counter1.setName("old");
        metricsRepository.save(counter1);
        Counter counter2 = new Counter();
        counter2.setTimestamp(new Date(500));
        counter2.setName("old");
        metricsRepository.save(counter2);
        Counter counter3 = new Counter();
        counter3.setTimestamp(new Date(1000));
        counter3.setName("new");
        metricsRepository.save(counter3);

        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.get("/metrics"))
            .andExpect(MockMvcResultMatchers.status().isOk()).andReturn();
    }


}
