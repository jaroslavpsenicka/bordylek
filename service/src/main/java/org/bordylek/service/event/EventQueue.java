package org.bordylek.service.event;

import org.bordylek.service.model.Event;

/**
 * Queue processing important application events such as user registration etc.
 * May be further processed, e.g. an email may be delivered somewhere etc.
 * @author jaroslav.psenicka@gmail.com
 */
public interface EventQueue {

	/**
	 * Enqueue an event.
	 * @param event event to send
	 */
	void send(Event event);

}
