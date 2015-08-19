package org.bordylek.service.event;

import org.bordylek.service.model.User;

@SuppressWarnings("serial")
public class NewUserEvent extends AbstractEvent {

    private User user;

	public static final String NAME = "newUser";

	public NewUserEvent(User user) {
		super(DOMAIN.USER, NAME);
		this.user = user;
	}

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
