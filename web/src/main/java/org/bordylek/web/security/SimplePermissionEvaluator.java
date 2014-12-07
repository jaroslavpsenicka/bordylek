package org.bordylek.web.security;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bordylek.service.model.Authored;
import org.bordylek.service.model.User;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;

public class SimplePermissionEvaluator implements PermissionEvaluator {

	private Map<String, OwnerEvaluator> ownerEvaluators;

	private static final Log LOG = LogFactory.getLog(SimplePermissionEvaluator.class);

	public void setOwnerEvaluators(Collection<OwnerEvaluator> evaluators) {
		this.ownerEvaluators = new HashMap<String, OwnerEvaluator>(evaluators.size());
		for (OwnerEvaluator e : evaluators) {
			Collection<Class<?>> applicableClasses = e.getApplicableClasses();
			if (applicableClasses != null) {
				for (Class<?> c : applicableClasses) {
					this.ownerEvaluators.put(c.getName(), e);
				}
			}
		}
	}

	public boolean hasPermission(Authentication auth, Object domainObject, Object perm) {
		LOG.debug("evaluating " + auth.getPrincipal() + "'s perm " + perm + " on " + domainObject);
		if ((domainObject != null) && ("OWNER".equalsIgnoreCase(perm.toString()))) {
			String cn = domainObject.getClass().getName();
			OwnerEvaluator oe = (OwnerEvaluator) this.ownerEvaluators.get(cn);
			if ((oe == null) && ((domainObject instanceof Authored))) {
				oe = (OwnerEvaluator) this.ownerEvaluators.get(Authored.class.getName());
			}
			if (oe != null) {
				boolean status = oe.isOwner(auth, domainObject);
				LOG.debug("rejecting " + auth.getName());
				return status;
			}
			throw new IllegalStateException("no evaluator found for " + domainObject);
		}
		LOG.debug("rejecting " + auth.getName() + ", not evaluated");
		return false;
	}

	public boolean hasPermission(Authentication auth, Serializable id, String type, Object perm) {
		throw new UnsupportedOperationException();
	}

	public static class UserOwnerEvaluator implements OwnerEvaluator {
		public Collection<Class<?>> getApplicableClasses() {
			return Arrays.asList(new Class<?>[] { User.class });
		}

		public boolean isOwner(Authentication auth, Object domainObject) {
			if (User.class.equals(domainObject.getClass())) {
				String userId = ((User) auth.getPrincipal()).getId();
				return userId != null && userId.equals(((User) domainObject).getId());
			}
			throw new IllegalStateException("illegal " + domainObject.getClass());
		}
	}
}
