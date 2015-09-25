package org.bordylek.mon;

import org.bordylek.mon.model.Alert;

public interface NotificationGateway {

    void send(Alert alert);
}
