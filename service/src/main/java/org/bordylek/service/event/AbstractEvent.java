package org.bordylek.service.event;

import org.bordylek.service.model.Event;

@SuppressWarnings("serial")
public abstract class AbstractEvent implements Event {

	private DOMAIN domain;
	private String name;

	public AbstractEvent(Event.DOMAIN domain, String eventName) {
		this.domain = domain;
		this.name = eventName;
	}

	public DOMAIN getDomain() {
		return domain;
	}
	
	public void setDomain(DOMAIN domain) {
		this.domain = domain;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String eventName) {
		this.name = eventName;
	}

}
