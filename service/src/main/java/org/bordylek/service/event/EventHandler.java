package org.bordylek.service.event;

import java.util.Map;

public interface EventHandler {
	void handleEvent(String eventDomain, String eventName, Map<String, Object> parameters);
}
