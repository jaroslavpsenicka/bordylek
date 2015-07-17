package org.bordylek.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResponseErrorHandler;

import java.io.IOException;

public class HTTPErrorCodeErrorHandler implements ResponseErrorHandler {

	@Override
	public void handleError(ClientHttpResponse response) throws IOException {
		if (response.getStatusCode() == HttpStatus.UNAUTHORIZED) {
			throw new UnauthorizedException();
		} else if (response.getStatusCode() == HttpStatus.FORBIDDEN) {
			throw new UnauthorizedException();
		} else if (response.getStatusCode() == HttpStatus.BAD_REQUEST) {
			throw new BadRequestException();
		} else if (response.getStatusCode() == HttpStatus.NOT_FOUND) {
			throw new NotFoundException();
		}
	}

	@Override
	public boolean hasError(ClientHttpResponse response) throws IOException {
		return response.getStatusCode().value() >= HttpStatus.BAD_REQUEST.value();
	}
	
	@SuppressWarnings("serial")
	public static class UnauthorizedException extends HttpClientErrorException {
		public UnauthorizedException() {
			super(HttpStatus.UNAUTHORIZED);
		}
	}

	@SuppressWarnings("serial")
	public static class BadRequestException extends HttpClientErrorException {
		public BadRequestException() {
			super(HttpStatus.BAD_REQUEST);
		}
	}

	@SuppressWarnings("serial")
	public static class NotFoundException extends HttpClientErrorException {
		public NotFoundException() {
			super(HttpStatus.NOT_FOUND);
		}
	}

}
