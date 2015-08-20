package org.bordylek.web;

import org.apache.commons.lang.StringUtils;
import org.bordylek.service.model.User;
import org.bordylek.service.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
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
public class UserTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
	private WebApplicationContext webApplicationContext;
	
	@Autowired
	private MongoTemplate mongoTemplate;

    @Autowired
    private InMemoryUserDetailsManager userDetailsManager;

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

    }

    @Test
    public void findMe() throws Exception {
        mockMvc.perform(get("/user/me"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id", is(user.getId())))
            .andExpect(jsonPath("name", is(user.getName())))
            .andExpect(jsonPath("email", is(user.getEmail())));
    }

    @Test
	public void findExistingUser() throws Exception {
        mockMvc.perform(get("/user/" + user.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id", is(user.getId())))
            .andExpect(jsonPath("name", is(user.getName())))
            .andExpect(jsonPath("email", is(user.getEmail())));
	}

    @Test
    public void findUnknownUser() throws Exception {
        mockMvc.perform(get("/user/unknown")).andExpect(status().isNotFound());
    }

    @Test(expected = ServletException.class)
    public void findAsNonAuthenticated() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        mockMvc.perform(get("/user/" + user.getId()));
    }

    @Test
    public void updateName() throws Exception {
        String content = "{\"name\": \"J.F. Doe\", \"location\": {\"id\": \"123\", \"name\": \"name\", \"lat\": 0.0, \"lng\": 0.0}}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("id", is(user.getId())))
            .andExpect(jsonPath("name", is("J.F. Doe")))
            .andExpect(jsonPath("email", is(user.getEmail())));
    }

    @Test
    public void cannotUpdateAnotherUser() throws Exception {
        User user2 = new User();
        user2.setRegId("GOOGLE/2");
        user2.setName("Mary Doe");
        user2.setEmail("mary@doe.com");
        user2.setCreateDate(new Date());
        user2 = userRepository.save(user2);

        String content = "{\"name\": \"Mary Doe\",  \"location\": {\"id\": \"123\", \"name\": \"name\", \"lat\": 0.0, \"lng\": 0.0}}";
        mockMvc.perform(post("/user/" + user2.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isForbidden());
    }

    @Test
    public void cannotUpdateWithoutName() throws Exception {
        String content = "{\"location\": \"1\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateWithEmptyName() throws Exception {
        String content = "{\"name\": \"\", \"location\": \"1\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateWithShortName() throws Exception {
        String content = "{\"name\": \"A\", \"location\": \"1\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateWithTooLongName() throws Exception {
        String content = "{\"name\": \"" + StringUtils.repeat("A", 256) + "\", \"location\": \"1\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateWithoutLocation() throws Exception {
        String content = "{\"name\": \"Mary Doe\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void cannotUpdateWithEmptyLocation() throws Exception {
        String content = "{\"name\": \"Mary Doe\", \"location\": \"\"}";
        mockMvc.perform(post("/user/" + user.getId()).content(content).contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
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
