package org.bordylek.service.event;

import org.bordylek.service.model.Community;

@SuppressWarnings("serial")
public class NewCommunityEvent extends AbstractEvent {

	public static final String NAME = "newCommunity";

	public NewCommunityEvent(Community community) {
		super(EventDomain.COMMUNITY, NAME);
		addParameter("community", community);
	}

}
