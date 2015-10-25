package org.bordylek.service.model.process;

import org.bordylek.service.NotFoundException;
import org.bordylek.service.repository.VotingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;

import java.util.Date;

public class VotingScoreChangeRequest implements ChangeRequest {

    @Autowired
    private VotingRepository votingRepository;

    private String votingId;
    private String userId;
    private VoteAnswer answer;

    private static final Logger LOG = LoggerFactory.getLogger(VotingScoreChangeRequest.class);

    public VotingScoreChangeRequest() {
    }

    public VotingScoreChangeRequest(String votingId, String userId, VoteAnswer answer) {
        Assert.notNull(votingId);
        Assert.notNull(userId);
        Assert.notNull(answer);
        this.votingId = votingId;
        this.userId = userId;
        this.answer = answer;
    }

    public String getVotingId() {
        return votingId;
    }

    public void setVotingId(String votingId) {
        this.votingId = votingId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public VoteAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(VoteAnswer answer) {
        this.answer = answer;
    }

    @Override
    public void execute() {
        LOG.debug("Processing voting " + votingId + " user " + userId);
        Voting voting = votingRepository.findOne(votingId);
        if (voting != null) {
            voting.getVotes().put(userId, new Vote(answer, new Date()));
            votingRepository.save(voting);
        } else throw new NotFoundException("voting " + votingId + " not found.");
    }
}
