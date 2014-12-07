package org.bordylek.web.security;

import java.util.Collection;

import org.springframework.security.core.Authentication;

public abstract interface OwnerEvaluator {
	public Collection<Class<?>> getApplicableClasses();
	public boolean isOwner(Authentication paramAuthentication, Object paramObject);
}
