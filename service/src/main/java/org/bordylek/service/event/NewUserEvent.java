package org.bordylek.service.event;

import org.bordylek.service.model.User;

@SuppressWarnings("serial")
public class NewUserEvent extends AbstractEvent {

	public static final String NAME = "newUser";

	public NewUserEvent(User user) {
		super(EventDomain.USER, NAME);
		addParameter("user", user);
	}

}
