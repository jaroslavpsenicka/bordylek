package org.bordylek.service.model;

import java.io.Serializable;
import java.util.Map;

public interface Event extends Serializable {
	
	String getDomainName();
	String getEventName();
	Object getParameter(String name);
	Map<String, Object> getParameters();
}