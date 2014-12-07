package org.bordylek.web;

import com.github.fakemongo.Fongo;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.bordylek.service.event.EventDomain;
import org.bordylek.service.event.JoinCommunityEvent;
import org.bordylek.service.event.NewCommunityEvent;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.bordylek.web.HTTPErrorCodeErrorHandler.BadRequestException;
import org.bordylek.web.HTTPErrorCodeErrorHandler.NotFoundException;
import org.bordylek.web.HTTPErrorCodeErrorHandler.UnauthorizedException;
import org.bordylek.web.client.CommunityTestClient;
import org.bordylek.web.client.UserTestClient;
import org.bordylek.web.security.RemoteUserAuthentication;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.DefaultAuthorizationRequest;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.Collections;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

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
	private TokenStore tokenStore;
	
	@Autowired
	private TestEventQueue testEventQueue;

	@Autowired
	private Fongo fongo;

	RestTemplate template;
	private HttpClient httpClient;
	private ServerConnector jetty;
	private CommunityTestClient communityClient;
	private UserTestClient userClient;
	private DBCollection communityCol;

	private static final String COMM_URL = "http://%s:%s/bordylek/service/comm";
	private static final String USER_URL = "http://%s:%s/bordylek/service/user";

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "community");
		mongoTemplate.indexOps(Community.class).ensureIndex(new GeospatialIndex("location"));
		httpClient = HttpClientBuilder.create().build();
		template = new RestTemplate(new HttpComponentsClientHttpRequestFactory(httpClient));
		template.setErrorHandler(new HTTPErrorCodeErrorHandler());
		userService.setEventQueue(testEventQueue);
		communityService.setEventQueue(testEventQueue);
		JettyServer jettyServer = new JettyServer();
		jettyServer.setApplicationContext(applicationContext);
		jetty = jettyServer.start();
		communityClient = new CommunityTestClient(COMM_URL, jetty.getHost(), jetty.getLocalPort());
		communityClient.setTemplate(template);
		userClient = new UserTestClient(USER_URL, jetty.getHost(), jetty.getLocalPort());
		userClient.setTemplate(template);
		communityCol = fongo.getDB("bordylek").getCollection("community");
	}
	
	@After
	public void after() throws Exception {
		OAuth2AccessToken token = tokenStore.readAccessToken("token1");
		if (token != null) tokenStore.removeAccessToken(token);
		jetty.shutdown();
	}

	/**
	 * Insert new community.
	 */
	@Test
	public void insert() throws Exception {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		testEventQueue.clear();
		
		Community comm = communityClient.insert(new Community("abc"));
		
		Assert.assertNotNull(comm);
		assertEquals(EventDomain.COMMUNITY, testEventQueue.getLastEvent().getDomainName());
		assertEquals(NewCommunityEvent.NAME, testEventQueue.getLastEvent().getEventName());
	}

	/**
	 * Try to insert new community, no name given.
	 */
	@Test(expected=BadRequestException.class)
	public void insertNullName() {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		communityClient.insert(new Community(null));
	}
	
	/**
	 * Insert new community.
	 */
	@Test
	public void insertAndFindNear() throws Exception {
		configureAuthentication("token1", "USER", new double[]{1.1, 1.1});
		communityClient.insert(new Community("c1", new double[] {1.0, 1.0}));

		DBObject dbObject = communityCol.findOne();
		assertEquals("c1", dbObject.get("title"));
	}

	/**
	 * Update community title.
	 */
	@Test
	public void update() throws Exception {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
        configureAuthentication("token1", "ADMIN");
		
		comm.setTitle("def");
		Community comm2 = communityClient.update(comm);
		Assert.assertNotNull(comm2);
		assertEquals("def", comm2.getTitle());
	}

	/**
	 * Try to update without authorization.
	 */
	@Test(expected=UnauthorizedException.class)
	public void updateNotAuthorized() throws Exception {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		OAuth2AccessToken token = tokenStore.readAccessToken("token1");
		if (token != null) tokenStore.removeAccessToken(token);

		comm.setTitle("def");
		communityClient.update(comm);
	}

	/**
	 * Delete community.
	 */
	@Test
	public void delete() throws Exception {
		configureAuthentication("token1", "ADMIN");
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());

        communityClient.delete(comm);

		DBObject dbObject = communityCol.findOne();
		assertNull(dbObject);
	}

	/**
	 * Try to delete without authorization.
	 */
	@Test(expected=UnauthorizedException.class)
	public void deleteNotAuthorized() throws Exception {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		OAuth2AccessToken token = tokenStore.readAccessToken("token1");
		if (token != null) tokenStore.removeAccessToken(token);

		communityClient.delete(comm);
	}

	/**
	 * User joins a community.
	 */
	@Test
	public void join() throws Exception {
		User user = configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		Community comm = communityClient.insert(new Community("abc"));
		Assert.assertNotNull(comm);
		Assert.assertNotNull(comm.getId());
		testEventQueue.clear();

		communityClient.join(comm);
		
		assertEquals(user.getId(), user.getId());
		assertEquals(comm.getId(), user.getJoinedCommunities().get(0));
		assertEquals(EventDomain.USER, testEventQueue.getLastEvent().getDomainName());
		assertEquals(JoinCommunityEvent.NAME, testEventQueue.getLastEvent().getEventName());
	}
	
	@Test(expected=NotFoundException.class)
	public void joinNonExisting() throws Exception {
		configureAuthentication("token1", "USER", new double[] {1.1, 1.1});
		Community comm2 = new Community("def");
		comm2.setId("1");

		communityClient.join(comm2);
	}

	private void configureAuthentication(String token, String role) {
		User user = new User("1", "john@doe.com", role);
		OAuth2Authentication auth = new OAuth2Authentication(
    		new DefaultAuthorizationRequest(token, Collections.singleton("read")), 
    		new RemoteUserAuthentication(user));
        tokenStore.storeAccessToken(new DefaultOAuth2AccessToken(token), auth);
	}

	private User configureAuthentication(String token, String role, double[] loc) {
		User user = new User("1", "john@doe.com", role);
		user.setLocation(loc);
		user.setCreateDate(new Date());
		OAuth2Authentication auth = new OAuth2Authentication(
    		new DefaultAuthorizationRequest(token, Collections.singleton("read")), 
    		new RemoteUserAuthentication(user));
        tokenStore.storeAccessToken(new DefaultOAuth2AccessToken(token), auth);
        return user;
	}
	
}
