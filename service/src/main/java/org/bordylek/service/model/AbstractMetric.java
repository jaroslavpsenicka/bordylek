package org.bordylek.service.model;

import org.springframework.data.annotation.Id;

import java.util.Date;

public abstract class AbstractMetric {

    @Id
    private String id;

    private String name;
    private Date timestamp;

    public AbstractMetric() {
    }

    public AbstractMetric(String name) {
        this.name = name;
        this.timestamp = new Date();
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

    public void setName(final String name) {
        this.name = name;
    }

    public Date getTimestamp() {
        if (timestamp != null) {
            return (Date) timestamp.clone();
        }
        return null;
    }

    public void setTimestamp(final Date timestamp) {
        if (timestamp != null) {
            this.timestamp = (Date) timestamp.clone();
        }
    }

}
