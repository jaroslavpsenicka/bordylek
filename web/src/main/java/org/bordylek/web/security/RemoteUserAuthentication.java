package org.bordylek.web.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bordylek.service.model.User;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

/**
 * Authentication token representing a user decoded from a UAA access token.
 */
@SuppressWarnings("serial")
public class RemoteUserAuthentication extends AbstractAuthenticationToken implements Authentication {

	private User user;

	private static Collection<? extends GrantedAuthority> createAuthorities(String[] roles) {
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		for (String role : roles) auths.add(new SimpleGrantedAuthority(role));
		return auths;
	}

	public RemoteUserAuthentication(User user) {
		super(createAuthorities(user.getRoles()));
		this.user = user;
		this.setAuthenticated(true);
	}

	@Override
	public Object getCredentials() {
		return "<N/A>";
	}

	@Override
	public Object getPrincipal() {
		return user;
	}

}