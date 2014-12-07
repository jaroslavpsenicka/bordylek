package org.bordylek.web;

import org.bordylek.service.event.EventQueue;
import org.bordylek.service.model.Event;

import java.util.ArrayList;
import java.util.List;

public class TestEventQueue implements EventQueue {

	private List<Event> events = new ArrayList<Event>();

	public void send(Event event) {
		this.events.add(event);
	}
	
	public void clear() {
		events.clear();
	}
	
	public List<Event> getEvents() {
		return events;
	}

	public Event getLastEvent() {
		if (events.isEmpty()) throw new IllegalStateException();
		return events.get(events.size() - 1);
	}

}
