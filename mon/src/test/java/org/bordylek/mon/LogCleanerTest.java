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

import java.util.Date;

import static org.junit.Assert.assertEquals;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/application-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml", "/integration-context.xml"})
public class LogCleanerTest {

    @Autowired
    private LogRepository logRepository;

	@Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    private LogCleaner logCleaner;

	@Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "monitor");
	}
	
	@After
	public void after() throws Exception {
    }

    @Test
    public void clean() throws Exception {
        logRepository.save(new Log("Log1", new Date(0)));
        logCleaner.clean();
        assertEquals(0, logRepository.findAll().size());
    }

}
