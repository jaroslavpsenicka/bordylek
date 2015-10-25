package org.bordylek.web;

import net.sf.ehcache.Ehcache;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.bordylek.service.model.process.Vote;
import org.bordylek.service.model.process.VoteAnswer;
import org.bordylek.service.model.process.Voting;
import org.bordylek.service.repository.CommunityRepository;
import org.bordylek.service.repository.UserRepository;
import org.bordylek.service.repository.VotingRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
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
import java.util.Map;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/application-context.xml",
    "/security-context.xml", "/social-context.xml", "/metrics-context.xml", "/test-context.xml"})
public class VotingTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private InMemoryUserDetailsManager userDetailsManager;

    @Autowired
    private CacheManager cacheManager;

    private MockMvc mockMvc;

    private User user;
    private Community community;
    private Voting voting;

    @Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "voting");
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

        community = new Community();
        community.setId("2");
        community.setTitle("Community");
        community.setCreatedBy(user);
        communityRepository.save(community);

        voting = new Voting();
        voting.setId("1");
        voting.setName("V1");
        voting.setCreator(user);
        voting.setCommunity(community);
        voting.setStartDate(new Date());
        voting.setEndDate(new Date(System.currentTimeMillis() + 10000));
        voting.setMinInterest(0.5);
        voting.setMinResult(0.75);
        votingRepository.save(voting);
	}
	
	@After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        userDetailsManager.deleteUser(user.getRegId());
        Ehcache cache = (Ehcache) cacheManager.getCache("votes").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void voteAndListVotingsForCommunity() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isOk());
        Thread.sleep(2000);

        mockMvc.perform(get("/votes/" + community.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is("1")))
            .andExpect(jsonPath("$[0].votes." + user.getId() + ".answer", is("YES")));
    }

    @Test
    public void listEmptyVotingsForCommunity() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(get("/votes/" + community.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id", is("1")))
            .andExpect(jsonPath("$[0].name", is("V1")));
    }

    @Test
    public void voteYes() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isOk());
        Thread.sleep(1000);

        Voting voting = votingRepository.findOne("1");
        Map<String, Vote> votes = voting.getVotes();
        assertEquals(1, votes.size());
        Vote vote = votes.get(user.getId());
        assertEquals(VoteAnswer.YES, vote.getAnswer());
        assertNotNull(vote.getVoteDate());
    }

    @Test
    public void voteNo() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"NO\"").header("Content-Type", "application/json"))
            .andExpect(status().isOk());
        Thread.sleep(1000);

        Voting voting = votingRepository.findOne("1");
        Map<String, Vote> votes = voting.getVotes();
        assertEquals(1, votes.size());
        Vote vote = votes.get(user.getId());
        assertEquals(VoteAnswer.NO, vote.getAnswer());
        assertNotNull(vote.getVoteDate());
    }

    @Test
    public void voteIllegal() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"ILLEGAL\"").header("Content-Type", "application/json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void voteLate() throws Exception {
        voting.setEndDate(new Date());
        votingRepository.save(voting);
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void voteUnknownVoting() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/illegal").content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isNotFound());
    }

    @Test(expected = AuthenticationCredentialsNotFoundException.class)
    public void voteUnauthenticated() throws Throwable {
        SecurityContextHolder.getContext().setAuthentication(null);
        try {
            mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"));
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
