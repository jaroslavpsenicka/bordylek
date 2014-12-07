package org.bordylek.web.client;

import org.bordylek.service.model.Community;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class CommunityTestClient {
	
	private String base;
	private HttpHeaders requestHeaders;
	private RestTemplate template;
	
	public CommunityTestClient(String url, String host, int port) {
		this.base = String.format(url, host, port);
		this.requestHeaders = new HttpHeaders();
		this.requestHeaders.set("Authorization", "Bearer token1");
	}

	public void setTemplate(RestTemplate template) {
		this.template = template;
	}

	public Community insert(Community comm) {
		HttpEntity<Community> request = new HttpEntity<>(comm, requestHeaders);
		ResponseEntity<Community> response = template.exchange(base, HttpMethod.POST, 
			request, Community.class);
		return response.getBody();
	}

	public Community[] find() {
		HttpEntity<Community[]> request = new HttpEntity<>(requestHeaders);
		ResponseEntity<Community[]> response = template.exchange(base, HttpMethod.GET, 
			request, Community[].class);
		return response.getBody();
	}

	public Community update(Community comm) {
		HttpEntity<Community> request = new HttpEntity<>(comm, requestHeaders);
		ResponseEntity<Community> response = template.exchange(base+"/"+comm.getId(), 
			HttpMethod.PUT, request, Community.class);
		return response.getBody();
	}

	public void delete(Community comm) {
		HttpEntity<?> request = new HttpEntity<Object>(requestHeaders);
		template.exchange(base+"/"+comm.getId(), HttpMethod.DELETE, request, Void.class);
	}

	public void join(Community comm) {
		HttpEntity<?> request = new HttpEntity<Object>(requestHeaders);
		template.exchange(base+"/"+comm.getId()+"/join", HttpMethod.POST, request, Void.class);
	}


}