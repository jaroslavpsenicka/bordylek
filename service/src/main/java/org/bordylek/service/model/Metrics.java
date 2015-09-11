package org.bordylek.service.model;

import org.springframework.data.annotation.Id;

import java.util.Date;

public abstract class Metrics {

    @Id
    private String id;

    private String name;
    private Date timestamp;

    public Metrics() {
    }

    public Metrics(String name, Date timestamp) {
        this.name = name;
        this.timestamp = timestamp;
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
