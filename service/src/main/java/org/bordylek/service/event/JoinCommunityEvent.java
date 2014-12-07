package org.bordylek.service.event;

import org.bordylek.service.model.Community;
import org.bordylek.service.model.User;

@SuppressWarnings("serial")
public class JoinCommunityEvent extends AbstractEvent {

	public static final String NAME = "joinCommunity";

	public JoinCommunityEvent(User user, Community community) {
		super(EventDomain.USER, NAME);
		addParameter("community", community);
		addParameter("user", user);
	}

}
