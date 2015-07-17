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
import org.bordylek.web.HTTPErrorCodeErrorHandler.UnauthorizedException;
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
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.token.TokenStore;
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
	private TokenStore tokenStore;

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
		user = new User();
		user.setReg(Registrar.GOOGLE);
		user.setRegId("1");
		user.setName("John Doe");
		user.setEmail("john@doe.com");
		userCol = fongo.getDB("bordylek").getCollection("user");
	}
	
	@After
	public void after() throws Exception {
		OAuth2AccessToken token = tokenStore.readAccessToken("token1");
		if (token != null) tokenStore.removeAccessToken(token);
		jetty.shutdown();
	}

	/**
	 * Register new user.
	 */
	@Test
	public void registerNewUser() throws Exception {
        configureAuthentication("token1", "john@doe.com", "USER");
        user = userClient.insert(user);
        Assert.assertNotNull(user);
		Assert.assertNotNull(user.getId());
		User user2 = userClient.find(user.getId());
		Assert.assertNotNull(user2);
		assertEquals(EventDomain.USER, testEventQueue.getLastEvent().getDomainName());
		assertEquals(NewUserEvent.NAME, testEventQueue.getLastEvent().getEventName());
	}

	/**
	 * Find non-existing user.
	 */
	@Test(expected=NotFoundException.class)
	public void findUnknownUser() throws Exception {
        configureAuthentication("token1", "john@doe.com", "USER");
        userClient.find("unknown");
	}
	
	/**
	 * Try to insert new user, no name given.
	 */
	@Test(expected=BadRequestException.class)
	public void insertNullName() {
        configureAuthentication("token1", "john@doe.com", "USER");
        user.setName(null);
        userClient.insert(user);
	}
	
	/**
	 * Update user name, admin user.
	 */
	@Test
	public void updateByOwner() throws Exception {
        User requestor = configureAuthentication("token1", "john@doe.com", "USER");
		user = userClient.insert(user);
		requestor.setId(user.getId());
		user.setName("John O'Doe");
		User user2 = userClient.update(user);
		Assert.assertNotNull(user2);
		assertEquals("John O'Doe", user2.getName());
	}

	/**
	 * Update user name, admin user.
	 */
	@Test
	public void updateByAdmin() throws Exception {
        configureAuthentication("token1", "john@doe.com", "USER");
		user = userClient.insert(user);
		user.setName("John O'Doe");
        configureAuthentication("token1", "john@doe.com", "ADMIN");
		User user2 = userClient.update(user);
		Assert.assertNotNull(user2);
		assertEquals("John O'Doe", user2.getName());
	}

	/**
	 * Try to update without authorization.
	 */
	@Test(expected=UnauthorizedException.class)
	public void updateNotAuthorized() throws Exception {
		userClient.update(user);
	}

	/**
	 * Delete user.
	 */
	@Test
	public void delete() throws Exception {
        configureAuthentication("token1", "john@doe.com", "USER");
        user = userClient.insert(user);
        configureAuthentication("token1", "admin@doe.com", "ADMIN");
        userClient.delete(user);

		DBObject dbObject = userCol.findOne();
		assertEquals("John Doe", dbObject.get("name"));
		assertTrue((Boolean) dbObject.get("deleted"));
	}

	/**
	 * Try to delete non-existing user.
	 */
	@Test(expected=NotFoundException.class)
	public void deleteUnknown() throws Exception {
        configureAuthentication("token1", "admin@doe.com", "ADMIN");
        userClient.delete(user);
	}

	/**
	 * Try to delete without authorization.
	 */
	@Test(expected=UnauthorizedException.class)
	public void deleteNotAuthorized() throws Exception {
		userClient.insert(user);
		OAuth2AccessToken token = tokenStore.readAccessToken("token1");
		tokenStore.removeAccessToken(token);
		userClient.delete(user);
	}

	private User configureAuthentication(String token, String email, String role) {
		User user = new User(email, email, role);
		user.setId("admin1");
//		OAuth2Authentication auth = new OAuth2Authentication(
//    		new DefaultAuthorizationRequest(token, Collections.singleton("read")),
//    		new RemoteUserAuthentication(user));
//        tokenStore.storeAccessToken(new DefaultOAuth2AccessToken(token), auth);
        return user;
	}


}
