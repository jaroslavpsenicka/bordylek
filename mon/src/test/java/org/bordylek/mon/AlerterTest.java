package org.bordylek.mon;

import org.bordylek.service.model.Counter;
import org.bordylek.service.model.Metrics;
import org.bordylek.service.repository.MetricsRepository;
import org.drools.KnowledgeBase;
import org.drools.definition.KnowledgePackage;
import org.drools.definition.rule.Rule;
import org.drools.runtime.StatelessKnowledgeSession;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/web-context.xml", "/security-context.xml",
        "/rules-context.xml", "/test-context.xml"})
public class AlerterTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
	private WebApplicationContext webApplicationContext;

    @Autowired
    private KnowledgeBase knowledgeBase;

    @Autowired
    private StatelessKnowledgeSession droolsSession;

	@Before
	public void before() throws Exception {
	}
	
	@After
	public void after() throws Exception {
    }

    @Test
    public void metadata() {
        KnowledgePackage knowledgePackage = knowledgeBase.getKnowledgePackage("org.bordylek.mon.rules");
        Rule rule = knowledgePackage.getRules().iterator().next();
        assertEquals("Name", rule.getName());
    }

    @Test
    public void simpleExecute() throws Exception {
        final Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(0));
        counter1.setName("very-old");
        final Counter counter2 = new Counter();
        counter2.setTimestamp(new Date(500));
        counter2.setName("old");
        final Counter counter3 = new Counter();
        counter3.setTimestamp(new Date(1000));
        counter3.setName("new");
        List<Metrics> metrics = new ArrayList<Metrics>() {{
            add(counter1);
            add(counter2);
            add(counter3);
        }};

        droolsSession.execute(metrics);
        assertEquals("very-old", counter1.getName());
        assertEquals("processed", counter2.getName());
        assertEquals("new", counter3.getName());
    }

    @Test
    public void alerter() {
        TestAlerter alerter = new TestAlerter();
        droolsSession.setGlobal("alerter", alerter);
        Counter counter1 = new Counter();
        counter1.setTimestamp(new Date(0));
        counter1.setName("very-old");
        droolsSession.execute(Collections.singletonList(counter1));
        assertEquals("too old", alerter.getInfo());
    }
}
