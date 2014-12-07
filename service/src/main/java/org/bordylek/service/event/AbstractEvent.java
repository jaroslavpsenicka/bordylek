package org.bordylek.service.event;

import org.bordylek.service.model.Event;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("serial")
public abstract class AbstractEvent implements Event {

	private String domainName;
	private String eventName;
	private Map<String, Object> parameters = new HashMap<String, Object>();

	public AbstractEvent(String domainName, String eventName) {
		this.domainName = domainName;
		this.eventName = eventName;
	}

	public String getDomainName() {
		return domainName;
	}
	
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}
	
	public String getEventName() {
		return eventName;
	}
	
	public void setEventName(String eventName) {
		this.eventName = eventName;
	}

	public Object getParameter(String name) {
		return parameters.get(name);
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}
	
	protected void addParameter(String key, Object value) {
		this.parameters.put(key, value);
	}
	
	public void setParameters(Map<String, Object> parameters) {
		this.parameters = parameters;
	}
}
