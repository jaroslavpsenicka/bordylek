package org.bordylek.mon;

import net.sf.ehcache.Ehcache;
import org.bordylek.service.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
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

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/application-context.xml", "/security-context.xml",
    "/rules-context.xml", "/test-context.xml", "/integration-context.xml"})
public class UserTest {

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

	@Before
	public void before() throws Exception {
		mongoTemplate.remove(new Query(), "user");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        Ehcache cache = (Ehcache) cacheManager.getCache("users").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void findUser() throws Exception {
        authenticate("User", "ROLE_USER");
        mockMvc.perform(get("/user/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("name", is("User")))
            .andExpect(jsonPath("roles.ROLE_USER", is(1)));
    }

    @Test
    public void findAdmin() throws Exception {
        authenticate("User", "ROLE_ADMIN");
        mockMvc.perform(get("/user/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("name", is("User")))
            .andExpect(jsonPath("roles.ROLE_ADMIN", is(1)));
    }

    private void authenticate(String username, final String role) {
        SecurityContext context = SecurityContextHolder.getContext();
        ArrayList<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>() {{
            add(new SimpleGrantedAuthority(role));
        }};
        org.springframework.security.core.userdetails.User user =
            new org.springframework.security.core.userdetails.User(username, "", authorities);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, "pwd", authorities));
    }

}
