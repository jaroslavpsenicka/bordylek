package org.bordylek.service.model.process;

import javax.validation.constraints.NotNull;
import java.util.Date;

public class Vote {

    @NotNull
    private VoteAnswer answer;

    @NotNull
    private Date voteDate;

    public Vote() {
    }

    public Vote(VoteAnswer answer, Date date) {
        this.answer = answer;
        this.voteDate = date;
    }

    public VoteAnswer getAnswer() {
        return answer;
    }

    public void setAnswer(VoteAnswer answer) {
        this.answer = answer;
    }

    public Date getVoteDate() {
        return voteDate;
    }

    public void setVoteDate(Date voteDate) {
        this.voteDate = voteDate;
    }
}
