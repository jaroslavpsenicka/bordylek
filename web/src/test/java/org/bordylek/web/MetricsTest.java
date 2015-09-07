package org.bordylek.web;

import net.sf.ehcache.Ehcache;
import org.bordylek.service.model.AbstractMetric;
import org.bordylek.service.model.Timer;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.MetricsRepository;
import org.bordylek.service.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/web-context.xml",
    "/security-context.xml", "/social-context.xml", "/metrics-context.xml", "/test-context.xml"})
public class MetricsTest {

    @Autowired
    private MetricsRepository metricsRepository;

    @Autowired
    private UserRepository userRepository;

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

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "user");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

		user = new User();
		user.setRegId("GOOGLE/1");
		user.setName("John Doe");
		user.setEmail("john@doe.com");
        user.setCreateDate(new Date());
        user = userRepository.save(user);
        authenticate(user.getRegId(), "ROLE_USER");
        userDetailsManager.createUser(new org.springframework.security.core.userdetails.User(
            user.getRegId(), "pwd", AuthorityUtils.createAuthorityList("ROLE_USER")));
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        userDetailsManager.deleteUser(user.getRegId());
        Ehcache cache = (Ehcache) cacheManager.getCache("users").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void findMeMetric() throws Exception {
        mockMvc.perform(get("/user/me")).andExpect(status().isOk());
        Thread.sleep(1500);
        List<AbstractMetric> findMeMetrics = metricsRepository.findByName(UserController.class.getName() + ".findMe");
        assertTrue(findMeMetrics.size() > 0);
        assertEquals("1", String.valueOf(((Timer) findMeMetrics.get(0)).getCount()));
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
