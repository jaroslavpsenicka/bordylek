package org.bordylek.mon;

import org.bordylek.mon.model.Chart;
import org.bordylek.mon.repository.ChartRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/application-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml", "/integration-context.xml"})
public class ChartTest {

    @Autowired
    private ChartRepository chartRepository;

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
    public void listCharts() throws Exception {
        chartRepository.save(new Chart("Chart1", "chart", "small"));
        mockMvc.perform(MockMvcRequestBuilders.get("/charts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Chart1")))
            .andExpect(jsonPath("$[0].serie", is("chart")))
            .andExpect(jsonPath("$[0].size", is("small")));
    }

    @Test
    public void load() throws Exception {
        MvcResult result = mockMvc.perform(MockMvcRequestBuilders.fileUpload("/charts/load")
            .file(new MockMultipartFile("file", "load.json", "application/json",
                "[{\"name\":\"Used\",\"serie\":\"memory.heap.used\",\"size\":\"col-md-3\"}]".getBytes())))
            .andExpect(status().isOk()).andReturn();
        List<Chart> charts = chartRepository.findAll();
        assertEquals(1, charts.size());
        assertEquals("Used", charts.get(0).getName());
        assertEquals("memory.heap.used", charts.get(0).getSerie());
        assertEquals("col-md-3", charts.get(0).getSize());
    }

    @Test
    public void loadWithoutFile() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.fileUpload("/charts/load")).andExpect(status().isBadRequest());
    }

    @Test
    public void save() throws Exception {
        chartRepository.save(new Chart("Chart1", "chart", "small"));
        mockMvc.perform(MockMvcRequestBuilders.get("/charts/save"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Chart1")))
            .andExpect(jsonPath("$[0].serie", is("chart")))
            .andExpect(jsonPath("$[0].size", is("small")));
    }

    @Test
    public void delete() throws Exception {
        Chart chart = chartRepository.save(new Chart("Chart1", "chart", "small"));
        mockMvc.perform(MockMvcRequestBuilders.delete("/charts/" + chart.getId()))
            .andExpect(status().isOk());
        assertEquals(0, chartRepository.findAll().size());
    }

    @Test
    public void create() throws Exception {
        String json = "{\"name\":\"Used\",\"serie\":\"memory.heap.used\",\"size\":\"col-md-3\"}";
        mockMvc.perform(MockMvcRequestBuilders.post("/charts")
            .header("Content-Type", "application/json")
            .content(json))
            .andExpect(status().isCreated());
        mockMvc.perform(MockMvcRequestBuilders.get("/charts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name", is("Used")))
            .andExpect(jsonPath("$[0].serie", is("memory.heap.used")))
            .andExpect(jsonPath("$[0].size", is("col-md-3")));
    }

}
