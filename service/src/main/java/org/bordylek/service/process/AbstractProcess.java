package org.bordylek.service.process;

import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "process")
public class AbstractProcess implements Process {

    @Id
    private String id;

    @NotNull
    private String name;

    @DBRef
    private User creator;

    @DBRef
    private Community community;

    @NotNull
    private Date createDate;

    @Min(1)
    private int duration;

    @Min(0)
    @Max(1)
    private double minInterest;

    @Min(0)
    @Max(1)
    private double minResult;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
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

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
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
}
