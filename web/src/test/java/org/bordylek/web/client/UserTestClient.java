package org.bordylek.web.client;

import org.bordylek.service.model.User;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class UserTestClient {

	private String base;
	private HttpHeaders requestHeaders;
	private RestTemplate template;
	
	public UserTestClient(String url, String host, int port) {
		this.base = String.format(url, host, port);
		requestHeaders = new HttpHeaders();
		requestHeaders.set("Authorization", "Bearer token1");
	}
	
	public void setTemplate(RestTemplate template) {
		this.template = template;
	}
	
	public User insert(User user) {
		HttpEntity<User> request = new HttpEntity<User>(user, requestHeaders);
		ResponseEntity<User> response = template.exchange(base, HttpMethod.POST, 
			request, User.class);
		return response.getBody();
	}

	public User[] find() {
		HttpEntity<?> request = new HttpEntity<Object>(requestHeaders);
		ResponseEntity<User[]> response = template.exchange(base, HttpMethod.GET, request, User[].class);
		return response.getBody();
	}

	public User find(String userId) {
		HttpEntity<?> request = new HttpEntity<Object>(requestHeaders);
		ResponseEntity<User> response = template.exchange(base+"/"+userId, HttpMethod.GET, request, User.class);
		return response.getBody();
	}

	public void delete(User user) {
		HttpEntity<User> request = new HttpEntity<User>(user, requestHeaders);
		template.exchange(base+"/"+user.getId(), HttpMethod.DELETE, request, Void.class);
	}

	public User update(User user) {
		HttpEntity<User> request = new HttpEntity<User>(user, requestHeaders);
		return template.exchange(base+"/"+user.getId(), HttpMethod.PUT, request, User.class).getBody();
	}

	
}
