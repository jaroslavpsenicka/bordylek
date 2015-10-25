package org.bordylek.service.model.process;

public interface ChangeRequest {

    /**
     * Execute the change.
     * Please note - the change is performed by ChangeRequestHandler in separate thread. All autowired fields
     * should be resolved prior the execution.
     */
    void execute();
}
