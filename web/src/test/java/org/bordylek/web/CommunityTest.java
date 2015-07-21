package org.bordylek.web;

import com.github.fakemongo.Fongo;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import org.bordylek.service.event.EventDomain;
import org.bordylek.service.event.NewCommunityEvent;
import org.bordylek.service.model.Community;
import org.bordylek.web.HTTPErrorCodeErrorHandler.BadRequestException;
import org.bordylek.web.HTTPErrorCodeErrorHandler.UnauthorizedException;
import org.bordylek.web.client.CommunityTestClient;
import org.bordylek.web.client.UserTestClient;
import org.bordylek.web.server.JettyServer;
import org.eclipse.jetty.server.ServerConnector;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.GeospatialIndex;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/web-context.xml", "/security-context.xml", "/test-context.xml"})
public class CommunityTest {

	@Autowired
	private UserController userService;

	@Autowired
	private CommunityController communityService;

	@Autowired
	private WebApplicationContext applicationContext;

	@Autowired
	private MongoTemplate mongoTemplate;
	
	@Autowired
	private TestEventQueue testEventQueue;

	@Autowired
	private Fongo fongo;

	RestTemplate template;
	private ServerConnector jetty;
	private CommunityTestClient communityClient;
	private UserTestClient userClient;
	private DBCollection communityCol;

	private static final String COMM_URL = "http://%s:%s/bordylek/service/comm";
	private static final String USER_URL = "http://%s:%s/bordylek/service/user";

	private static final String USER = "user:test";
	private static final String ADMIN = "admin:test";

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "community");
		mongoTemplate.indexOps(Community.class).ensureIndex(new GeospatialIndex("location"));

		template = new RestTemplate();
		template.setErrorHandler(new HTTPErrorCodeErrorHandler());

		userService.setEventQueue(testEventQueue);
		communityService.setEventQueue(testEventQueue);
		JettyServer jettyServer = new JettyServer();
		jettyServer.setApplicationContext(applicationContext);
		jetty = jettyServer.start();

		communityClient = new CommunityTestClient(COMM_URL, jetty.getHost(), jetty.getLocalPort());
		communityClient.setTemplate(template);
		communityClient.setCredentials(ADMIN);

		communityCol = fongo.getDB("bordylek").getCollection("community");

		testEventQueue.clear();
	}
	
	@After
	public void after() throws Exception {
		jetty.shutdown();
	}

	@Test
	public void insert() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		assertEquals(EventDomain.COMMUNITY, testEventQueue.getLastEvent().getDomainName());
		assertEquals(NewCommunityEvent.NAME, testEventQueue.getLastEvent().getEventName());
	}

	@Test(expected=BadRequestException.class)
	public void insertInvalid() {
		communityClient.insert(new Community(null));
	}
	
	@Test
	public void insertAndFindNear() throws Exception {
		communityClient.insert(new Community("c1", new double[] {1.0, 1.0}));
		assertEquals("c1", communityCol.findOne().get("title"));
	}

	@Test
	public void update() throws Exception {
		communityClient.setCredentials(USER);
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		communityClient.setCredentials(ADMIN);

		comm.setTitle("def");
		Community comm2 = communityClient.update(comm);
		Assert.assertNotNull(comm2);
		assertEquals("def", comm2.getTitle());
	}

	@Test(expected=BadRequestException.class)
	public void updateInvalid() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());

		comm.setTitle(null);
		communityClient.update(comm);
	}

	@Test(expected=UnauthorizedException.class)
	public void updateNotAuthorized() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		communityClient.setCredentials(USER);

		comm.setTitle("def");
		communityClient.update(comm);
	}

	@Test(expected=UnauthorizedException.class)
	public void updateUnknown() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		communityClient.setCredentials("illegal:wrong");

		comm.setTitle("def");
		communityClient.update(comm);
	}

	@Test
	public void delete() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());

        communityClient.delete(comm.getId());

		DBObject dbObject = communityCol.findOne();
		assertNull(dbObject);
	}

	@Test(expected=UnauthorizedException.class)
	public void deleteNotAuthorized() throws Exception {
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		communityClient.setCredentials("illegal:wrong");

		communityClient.delete(comm.getId());
	}

}
