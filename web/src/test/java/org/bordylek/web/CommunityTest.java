package org.bordylek.web;

import net.sf.ehcache.Ehcache;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.Location;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.util.NestedServletException;

import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/web-context.xml",
    "/security-context.xml", "/social-context.xml", "/test-context.xml"})
public class CommunityTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    private InMemoryUserDetailsManager userDetailsManager;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc mockMvc;
    private User user;
    private Community communityPrague;

    public static final double PRAGUE_LAT = 50.0;
    public static final double PRAGUE_LNG = 14.0;

    @Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "user");
        mongoTemplate.remove(new Query(), "community");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		user = new User();
		user.setRegId("GOOGLE/1");
		user.setName("John Doe");
		user.setEmail("john@doe.com");
        user.setCreateDate(new Date());
        user.setLocation(new Location("Praha", PRAGUE_LAT, PRAGUE_LNG));
        user = userRepository.save(user);
        authenticate(user.getRegId(), "ROLE_USER");
        userDetailsManager.createUser(new org.springframework.security.core.userdetails.User(
                user.getRegId(), "pwd", AuthorityUtils.createAuthorityList("ROLE_USER")));

        communityPrague = new Community();
        communityPrague.setTitle("C1");
        communityPrague.setLocation(new Point(PRAGUE_LNG, PRAGUE_LAT));
        communityRepository.save(communityPrague);
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        userDetailsManager.deleteUser(user.getRegId());
        Ehcache cache = (Ehcache) cacheManager.getCache("comms").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void findOne() throws Exception {
        mockMvc.perform(get("/comm/" + communityPrague.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id", is(communityPrague.getId())))
            .andExpect(jsonPath("title", is(communityPrague.getTitle())));
    }

    @Test
    public void findOneCache() throws Exception {
        mockMvc.perform(get("/comm/" + communityPrague.getId())).andExpect(status().isOk());
        Ehcache cache = (Ehcache) cacheManager.getCache("comms").getNativeCache();
        assertNotNull(cache.get(communityPrague.getId()));
    }

    @Test
    @Ignore
    public void findNearby() throws Exception {
        mockMvc.perform(get("/comm"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void distantCommunity() throws Exception {
        double dist = new Distance(21, Metrics.KILOMETERS).getNormalizedValue();
        User user2 = new User();
        user2.setRegId("GOOGLE/2");
        user2.setName("John Distant");
        user2.setEmail("distant@doe.com");
        user2.setCreateDate(new Date());
        user2.setLocation(new Location("Faraway", PRAGUE_LAT - dist, PRAGUE_LNG - dist));
        user2 = userRepository.save(user2);
        userRepository.save(user2);
        authenticate(user2.getRegId(), "ROLE_USER");
        mockMvc.perform(get("/comm"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    @Ignore
    public void sosoCommunity() throws Exception {
        double dist = new Distance(19, Metrics.KILOMETERS).getNormalizedValue();
        User user2 = new User();
        user2.setRegId("GOOGLE/3");
        user2.setName("John Soso");
        user2.setEmail("john@doe.com");
        user2.setCreateDate(new Date());
        user2.setLocation(new Location("Soso", PRAGUE_LAT - dist, PRAGUE_LNG - dist));
        user2 = userRepository.save(user2);
        userRepository.save(user2);
        authenticate(user2.getRegId(), "ROLE_USER");
        mockMvc.perform(get("/comm"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    public void illegalComunityId() throws Exception {
        mockMvc.perform(get("/comm/illegal"))
            .andExpect(status().isNotFound());
    }

    @Test(expected = AccessDeniedException.class)
    public void illegalUserRole() throws Throwable {
        authenticate(user.getRegId(), "ROLE_XXX");
        try {
            mockMvc.perform(get("/comm/" + communityPrague.getId()));
        } catch (NestedServletException ex) {
            throw ex.getCause();
        }
    }

    private void authenticate(String email, final String role) {
        SecurityContext context = SecurityContextHolder.getContext();
        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>() {{
            add(new SimpleGrantedAuthority(role));
        }};
        org.springframework.security.core.userdetails.User user =
            new org.springframework.security.core.userdetails.User(email, "", authorities);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, "pwd", authorities));
    }

}
