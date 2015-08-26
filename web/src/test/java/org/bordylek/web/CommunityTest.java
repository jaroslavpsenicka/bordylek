package org.bordylek.web;

import org.apache.commons.lang.StringUtils;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
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

import javax.servlet.ServletException;
import java.util.ArrayList;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    private MockMvc mockMvc;
    private User user;
    private Community community;

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
        user = userRepository.save(user);
        authenticate(user.getRegId(), "ROLE_USER");
        userDetailsManager.createUser(new org.springframework.security.core.userdetails.User(
                user.getRegId(), "pwd", AuthorityUtils.createAuthorityList("ROLE_USER")));

        community = new Community();
        community.setTitle("C1");
        communityRepository.save(community);
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        userDetailsManager.deleteUser(user.getRegId());
    }

    @Test
    public void findComunity() throws Exception {
        mockMvc.perform(get("/comm/" + community.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id", is(community.getId())))
            .andExpect(jsonPath("title", is(community.getTitle())));
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
            mockMvc.perform(get("/comm/" + community.getId()));
        } catch (NestedServletException ex) {
            throw ex.getCause();
        }
    }

    private void authenticate(String email, final String role) {
        SecurityContext context = SecurityContextHolder.getContext();
        ArrayList authorities = new ArrayList() {{
            add(new SimpleGrantedAuthority(role));
        }};
        org.springframework.security.core.userdetails.User user =
            new org.springframework.security.core.userdetails.User(email, "", authorities);
        context.setAuthentication(new UsernamePasswordAuthenticationToken(user, "pwd", authorities));
    }

}
