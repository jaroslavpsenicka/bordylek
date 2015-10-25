package org.bordylek.service;

import org.bordylek.service.model.process.ChangeRequest;

public interface RequestGateway {

    void sendRequest(ChangeRequest request);

}
