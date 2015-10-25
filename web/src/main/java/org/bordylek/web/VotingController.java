package org.bordylek.web;

import org.bordylek.service.ExpiredException;
import org.bordylek.service.NotFoundException;
import org.bordylek.service.RequestGateway;
import org.bordylek.service.model.User;
import org.bordylek.service.model.process.VoteAnswer;
import org.bordylek.service.model.process.Voting;
import org.bordylek.service.model.process.VotingScoreChangeRequest;
import org.bordylek.service.repository.UserRepository;
import org.bordylek.service.repository.VotingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;

@Controller
public class VotingController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private VotingRepository votingRepository;

    @Autowired
    private RequestGateway requestGateway;

    private static final Logger LOG = LoggerFactory.getLogger(VotingController.class);

    @ResponseBody
    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value="/votes/{cid}", method = RequestMethod.GET, produces = "application/json")
    public List<Voting> getVotings(@PathVariable("cid") String communityId) {
        return votingRepository.findByCommunity(communityId);
    }

    @PreAuthorize("hasRole('USER')")
    @RequestMapping(value="/vote/{vid}", method = RequestMethod.POST, consumes = "application/json")
    public void vote(@PathVariable("vid") String votingId, @RequestBody VoteAnswer answer) throws ExpiredException {
        Voting voting = votingRepository.findOne(votingId);
        if (voting == null) throw new NotFoundException(votingId);
        if (voting.getEndDate().getTime() > System.currentTimeMillis()) {
            String userId = getUser().getId();
            LOG.debug("Issuing VotingScoreChangeRequest voting " + votingId + " user " + userId);
            requestGateway.sendRequest(new VotingScoreChangeRequest(votingId, userId, answer));
        } else throw new ExpiredException(votingId);
    }

    @ExceptionHandler(NotFoundException.class)
    public void handleNotFoundException(Exception ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.NOT_FOUND.value());
    }

    @ExceptionHandler(ExpiredException.class)
    public void handleExpiredException(Exception ex, HttpServletResponse response) {
        response.setStatus(HttpStatus.BAD_REQUEST.value());
    }

    private User getUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String principal = (auth.getPrincipal() instanceof org.springframework.security.core.userdetails.User)
            ? ((org.springframework.security.core.userdetails.User) auth.getPrincipal()).getUsername()
            : auth.getPrincipal().toString();
        return this.userRepository.findByRegId(principal);
    }
}
