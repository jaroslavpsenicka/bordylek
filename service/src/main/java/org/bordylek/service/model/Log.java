package org.bordylek.service.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;

import java.util.Date;

public class Log {

    @Id
    @JsonIgnore
    private String id;

    private String message;
    private Date timestamp;

    public Log() {
    }

    public Log(String message, Date saveDate) {
        this.message = message;
        this.timestamp = saveDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }
}
