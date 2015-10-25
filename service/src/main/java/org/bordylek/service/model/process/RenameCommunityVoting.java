package org.bordylek.service.model.process;

import javax.validation.constraints.NotNull;

public class RenameCommunityVoting extends Voting {

    @NotNull
    private String entityId;

    @NotNull
    private String oldValue;

    @NotNull
    private String newValue;

    public String getEntityId() {
        return entityId;
    }

    public void setEntityId(String entityId) {
        this.entityId = entityId;
    }

    public String getOldValue() {
        return oldValue;
    }

    public void setOldValue(String oldValue) {
        this.oldValue = oldValue;
    }

    public String getNewValue() {
        return newValue;
    }

    public void setNewValue(String newValue) {
        this.newValue = newValue;
    }
}
