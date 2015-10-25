package org.bordylek.service.model.process;

import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Document(collection = "voting")
public class Voting {

    @Id
    private String id;

    @NotNull
    private String name;

    @DBRef
    private User creator;

    @DBRef
    private Community community;

    @NotNull
    private Date startDate;

    @NotNull
    private Date endDate;

    @Min(0)
    @Max(1)
    private double minInterest;

    @Min(0)
    @Max(1)
    private double minResult;

    private Map<String, Vote> votes;

    public Voting() {
        this.votes = new HashMap<>();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public double getMinInterest() {
        return minInterest;
    }

    public void setMinInterest(double minInterest) {
        this.minInterest = minInterest;
    }

    public double getMinResult() {
        return minResult;
    }

    public void setMinResult(double minResult) {
        this.minResult = minResult;
    }

    public Map<String, Vote> getVotes() {
        return votes;
    }

    public void setVotes(Map<String, Vote> votes) {
        this.votes = votes;
    }
}
