package org.bordylek.service.event;

import org.bordylek.service.model.Community;

@SuppressWarnings("serial")
public class NewCommunityEvent extends AbstractEvent {

    private Community community;

	public static final String NAME = "newCommunity";

	public NewCommunityEvent(Community community) {
		super(DOMAIN.COMMUNITY, NAME);
		this.community = community;
	}

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(Community community) {
        this.community = community;
    }
}
