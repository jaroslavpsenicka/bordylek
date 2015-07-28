package org.bordylek.service.event;

import org.bordylek.service.model.User;

@SuppressWarnings("serial")
public class UpdateUserEvent extends AbstractEvent {

	public static final String NAME = "updateUser";

	public UpdateUserEvent(User user) {
		super(EventDomain.USER, NAME);
		addParameter("user", user);
	}

}
