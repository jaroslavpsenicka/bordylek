package org.bordylek.service.model;

import java.io.Serializable;

public interface Event extends Serializable {

    enum DOMAIN {
        USER, COMMUNITY
    }

	DOMAIN getDomain();
	String getName();
}