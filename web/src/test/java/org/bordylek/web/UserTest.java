package org.bordylek.web;

import com.github.fakemongo.Fongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bordylek.service.event.EventDomain;
import org.bordylek.service.event.NewUserEvent;
import org.bordylek.service.model.Registrar;
import org.bordylek.service.model.User;
import org.bordylek.web.HTTPErrorCodeErrorHandler.BadRequestException;
import org.bordylek.web.HTTPErrorCodeErrorHandler.NotFoundException;
import org.bordylek.web.client.UserTestClient;
import org.bordylek.web.server.JettyServer;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/web-context.xml", "/security-context-basic.xml", "/test-context.xml"})
public class UserTest {

	@Autowired
	private UserController userService;
	
	@Autowired
	private WebApplicationContext applicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private TestEventQueue testEventQueue;

	@Autowired
	private Fongo fongo;

	private RestTemplate template;
	private HttpClient httpClient;
	private ServerConnector jetty;

	private User user;
	private UserTestClient userClient;
	private DBCollection userCol;

	private static final String USER_URL = "http://%s:%s/bordylek/service/user";
	private static final String USER = "user:test";
	private static final String ADMIN = "admin:test";

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "user");
		httpClient = HttpClientBuilder.create().build();
		template = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
		template.setErrorHandler(new HTTPErrorCodeErrorHandler());
		userService.setEventQueue(testEventQueue);
		JettyServer jettyServer = new JettyServer();
		jettyServer.setApplicationContext(applicationContext);
		jetty = jettyServer.start();

		userClient = new UserTestClient(USER_URL, jetty.getHost(), jetty.getLocalPort());
		userClient.setTemplate(template);
		userClient.setCredentials(USER);

		user = new User();
		user.setReg(Registrar.GOOGLE);
		user.setRegId("1");
		user.setName("John Doe");
		user.setEmail("john@doe.com");
		userCol = fongo.getDB("bordylek").getCollection("user");
	}
	
	@After
	public void after() throws Exception {
		jetty.shutdown();
	}

	@Test
	public void registerNewUser() throws Exception {
        user = userClient.insert(user);
        Assert.assertNotNull(user);
		Assert.assertNotNull(user.getId());
		User user2 = userClient.find(user.getId());
		Assert.assertNotNull(user2);
		assertEquals(EventDomain.USER, testEventQueue.getLastEvent().getDomainName());
		assertEquals(NewUserEvent.NAME, testEventQueue.getLastEvent().getEventName());
	}

	@Test(expected=NotFoundException.class)
	public void findUnknownUser() throws Exception {
        userClient.find("unknown");
	}
	
	@Test(expected=BadRequestException.class)
	public void insertNullName() {
        user.setName(null);
        userClient.insert(user);
	}
	
	@Test
	@Ignore
	public void updateByOwner() throws Exception {
		user = userClient.insert(user);
		user.setName("John O'Doe");
		User user2 = userClient.update(user);
		Assert.assertNotNull(user2);
		assertEquals("John O'Doe", user2.getName());
	}

	@Test
	public void updateByAdmin() throws Exception {
		user = userClient.insert(user);
		user.setName("John O'Doe");
		userClient.setCredentials(ADMIN);
		User user2 = userClient.update(user);
		Assert.assertNotNull(user2);
		assertEquals("John O'Doe", user2.getName());
	}

	@Test
	public void delete() throws Exception {
        user = userClient.insert(user);
		userClient.setCredentials(ADMIN);
        userClient.delete(user);

		DBObject dbObject = userCol.findOne();
		assertEquals("John Doe", dbObject.get("name"));
		assertTrue((Boolean) dbObject.get("deleted"));
	}

	@Test(expected=NotFoundException.class)
	public void deleteUnknown() throws Exception {
		userClient.setCredentials(ADMIN);
        userClient.delete(user);
	}

}
