package org.bordylek.service.event;

import org.bordylek.service.model.Event;

/**
 * @author jaroslav.psenicka@gmail.com
 */
public interface EventGateway {

    void send(Event event);

}
