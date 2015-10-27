package org.bordylek.web;

import net.sf.ehcache.Ehcache;
import org.bordylek.service.model.Community;
import org.bordylek.service.model.CommunityRef;
import org.bordylek.service.model.User;
import org.bordylek.service.model.process.RenameCommunityVoting;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebAppConfiguration  
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"/service-context.xml", "/integration-context.xml", "/application-context.xml",
    "/security-context.xml", "/social-context.xml", "/metrics-context.xml", "/test-context.xml"})
public class RenameCommunityVotingTest {

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
    private RenameCommunityVoting voting;

    @Before
	public void before() throws Exception {
        mongoTemplate.remove(new Query(), "voting");
        mongoTemplate.remove(new Query(), "user");
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext).build();

        user = createUser("GOOGLE/1", "John Doe", "john@doe.com");
        authenticate(user.getRegId(), "ROLE_USER");
        userDetailsManager.createUser(new org.springframework.security.core.userdetails.User(
            user.getRegId(), "pwd", AuthorityUtils.createAuthorityList("ROLE_USER")));

        community = new Community();
        community.setId("2");
        community.setTitle("Community");
        community.setCreatedBy(user);
        communityRepository.save(community);
	}

    @After
	public void after() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(null);
        userDetailsManager.deleteUser(user.getRegId());
        Ehcache cache = (Ehcache) cacheManager.getCache("votes").getNativeCache();
        cache.removeAll();
    }

    @Test
    public void directUpdate() throws Exception {
        user.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user);
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/comm/" + community.getId() + "/rename")
            .content("Community 2").header("Content-Type", "application/json"))
            .andExpect(status().isOk());

        assertEquals(0, votingRepository.findByCommunity(community.getId()).size());
    }

    @Test
    public void startVoting() throws Exception {
        user.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user);
        User user2 = createUser("GOOGLE/2", "Mary Doe", "mary@doe.com");
        user2.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user2);

        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/comm/" + community.getId() + "/rename")
            .content("Community 2").header("Content-Type", "application/json"))
            .andExpect(status().isCreated());
        Thread.sleep(1000);

        List<Voting> votings = votingRepository.findByCommunity(community.getId());
        assertEquals(1, votings.size());
        RenameCommunityVoting voting = (RenameCommunityVoting) votings.get(0);
        assertEquals(user.getId(), voting.getCreator().getId());
        assertEquals(community.getId(), voting.getCommunity().getId());
        assertEquals(new Double(0.5), new Double(voting.getMinInterest()));
        assertEquals(new Double(0.5), new Double(voting.getMinResult()));
        assertEquals("Community", voting.getOldValue());
        assertEquals("Community 2", voting.getNewValue());
        assertEquals(0, voting.getVotes().size());
    }

    @Test
    public void startVotingAndVote() throws Exception {
        user.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user);
        User user2 = createUser("GOOGLE/2", "Mary Doe", "mary@doe.com");
        user2.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user2);

        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/comm/" + community.getId() + "/rename")
            .content("Community 2").header("Content-Type", "application/json"))
            .andExpect(status().isCreated());

        List<Voting> votings = votingRepository.findByCommunity(community.getId());
        assertEquals(1, votings.size());
        RenameCommunityVoting voting = (RenameCommunityVoting) votings.get(0);
        assertEquals(0, voting.getVotes().size());

        authenticate(user2.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isOk());
        Thread.sleep(1000);

        votings = votingRepository.findByCommunity(community.getId());
        voting = (RenameCommunityVoting) votings.get(0);
        assertEquals(1, voting.getVotes().size());
        assertEquals(VoteAnswer.YES, voting.getVotes().get(user2.getId()).getAnswer());
    }

    @Test
    public void voteAndApplyChange() throws Exception {
        user.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user);
        User user2 = createUser("GOOGLE/2", "Mary Doe", "mary@doe.com");
        user2.getCommunities().add(new CommunityRef(community.getId(), community.getTitle()));
        userRepository.save(user2);

        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/comm/" + community.getId() + "/rename")
            .content("Community 2").header("Content-Type", "application/json"))
            .andExpect(status().isCreated());

        authenticate(user2.getRegId(), "ROLE_USER");
        Voting voting = votingRepository.findByCommunity(community.getId()).get(0);
        mockMvc.perform(post("/vote/" + voting.getId()).content("\"YES\"").header("Content-Type", "application/json"))
            .andExpect(status().isOk());
        Thread.sleep(1000);

        // TODO apply change test
    }

    @Test
    public void notCommunityMember() throws Exception {
        authenticate(user.getRegId(), "ROLE_USER");
        mockMvc.perform(post("/comm/" + community.getId() + "/rename")
            .content("Community 2").header("Content-Type", "application/json"))
            .andExpect(status().isForbidden());
    }

    private User createUser(String regId, String name, String email) {
        User user = new User();
        user.setRegId(regId);
        user.setName(name);
        user.setEmail(email);
        user.setCreateDate(new Date());
        return userRepository.save(user);
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
