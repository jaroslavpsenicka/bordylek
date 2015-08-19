package org.bordylek.web;

import com.mongodb.DBObject;
import org.bordylek.service.event.EventGateway;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.Event;
import org.bordylek.service.model.User;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Date;

import static org.junit.Assert.assertEquals;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml"})
public class EventTest {

    @Autowired
    private EventGateway eventGateway;

	@Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    @Qualifier("events")
    private PollableChannel eventsChannel;

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
    }

    @Test
    public void userCreated() throws Exception {
        eventGateway.send(new NewUserEvent(user));
        DBObject event = (DBObject) mongoTemplate.getCollection("events").find().toArray().get(1).get("payload");
        assertEquals("USER", event.get("domain"));
        assertEquals("newUser", event.get("name"));
        assertEquals("John Doe", ((DBObject)event.get("user")).get("name"));

        Message<Event> message = (Message<Event>) eventsChannel.receive();
        assertEquals(Event.DOMAIN.USER, message.getPayload().getDomain());
        assertEquals(NewUserEvent.NAME, message.getPayload().getName());

        assertEquals(1, mongoTemplate.getCollection("events").count());
    }

}
