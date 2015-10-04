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

    private String log;

    public Alert() {
        this(null, new Date(), null, null);
    }

    public Alert(String fqName, Severity severity, String message) {
        this(fqName, new Date(), severity, message);
    }

    public Alert(String fqName, Date timestamp, Severity severity, String message) {
        this.fqName = fqName;
        this.timestamp = timestamp;
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

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }
}
