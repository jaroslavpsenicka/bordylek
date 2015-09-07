package org.bordylek.web;

import org.bordylek.service.event.EventGateway;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.mail.internet.MimeMessage;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/metrics-context.xml", "/test-context.xml"})
public class EventTest {

    @Autowired
    private EventGateway eventGateway;

	@Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    private TestMailSender mailSender;

    private User user;

    @Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "events");
        user = new User();
        user.setRegId("GOOGLE/1");
        user.setName("John Doe");
        user.setEmail("john@doe.com");
        user.setCreateDate(new Date());
	}
	
	@After
	public void after() throws Exception {
        mailSender.clear();
    }

    @Test
    public void userCreated() throws Exception {
        eventGateway.send(new NewUserEvent(user));
        Thread.sleep(2000);
        List<MimeMessage> messages = mailSender.getMessages();
        assertEquals(1, messages.size());
        assertTrue(messages.get(0).getContent().toString().indexOf("Welcome to Bordylek, John Doe") > -1);
    }

}
