package org.bordylek.service.model.metrics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class Metrics {

    @Id
    @JsonIgnore
    private String id;

    private String name;
    private Date timestamp;

    private String logId;

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

    public String getLogId() {
        return logId;
    }

    public void setLogId(String logId) {
        this.logId = logId;
    }
}
