package org.bordylek.mon;

import org.bordylek.service.model.Counter;
import org.bordylek.service.model.Meter;
import org.bordylek.service.model.Metrics;
import org.bordylek.service.repository.MetricsRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
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

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/application-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml", "/integration-context.xml"})
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

        Metrics metric = metricsRepository.findTopByOrderByTimestampDesc();
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

    @Test
    public void latestMetricsByType() throws Exception {
        Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(0));
        counter1.setName("counter1");
        metricsRepository.save(counter1);
        Counter counter2 = new Counter();
        counter2.setTimestamp(new Date(0));
        counter2.setName("counter2");
        metricsRepository.save(counter2);
        Meter meter = new Meter();
        meter.setTimestamp(new Date(0));
        meter.setName("meter");
        metricsRepository.save(meter);

        mockMvc.perform(MockMvcRequestBuilders.get("/metrics/counter"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("data", hasSize(2)))
            .andExpect(jsonPath("data[0].name", is("counter1")))
            .andExpect(jsonPath("data[1].name", is("counter2")));
        mockMvc.perform(MockMvcRequestBuilders.get("/metrics/meter"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("data", hasSize(1)))
            .andExpect(jsonPath("data[0].name", is("meter")));
        mockMvc.perform(MockMvcRequestBuilders.get("/metrics/unknown"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void latestMetricsByTypeAndName() throws Exception {
        Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(System.currentTimeMillis() - 1000));
        counter1.setName("ccc");
        metricsRepository.save(counter1);
        Counter counter2 = new Counter();
        counter2.setTimestamp(new Date());
        counter2.setName("ccc");
        metricsRepository.save(counter2);
        Meter meter = new Meter();
        meter.setTimestamp(new Date());
        meter.setName("meter");
        metricsRepository.save(meter);

        mockMvc.perform(MockMvcRequestBuilders.get("/metrics/counter/ccc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("data", hasSize(2)))
            .andExpect(jsonPath("data[0].name", is("ccc")))
            .andExpect(jsonPath("data[1].name", is("ccc")));
        mockMvc.perform(MockMvcRequestBuilders.get("/metrics/counter/ccc").param("period", "500"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("data", hasSize(1)))
            .andExpect(jsonPath("data[0].name", is("ccc")));
    }

}
