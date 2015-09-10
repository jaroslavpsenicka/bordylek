package org.bordylek.mon.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import javax.validation.constraints.NotNull;
import java.util.Date;

@Document(collection = "alerts")
public class Alert {

    @Id
    private String id;

    @NotNull
    private Date timestamp;

    private String fqName;
    private Severity severity;
    private String message;
    private boolean resolved;

    public Alert() {
        this.timestamp = new Date();
    }

    public Alert(String fqName, Date timestamp, Severity severity, String message) {
        this.fqName = fqName;
        this.timestamp = new Date();
        this.severity = severity;
        this.message = message;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public String getFqName() {
        return fqName;
    }

    public void setFqName(String fqName) {
        this.fqName = fqName;
    }

    public Severity getSeverity() {
        return severity;
    }

    public void setSeverity(Severity severity) {
        this.severity = severity;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean getResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }
}
