package org.bordylek.web.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.codec.Base64;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.AccessTokenConverter;
import org.springframework.security.oauth2.provider.token.RemoteTokenServices;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestOperations;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

/**
 * Copied from Spring Security OAuth2 to support the custom format for a Google Token which is different from what Spring supports
 */
public class GoogleTokenServices extends RemoteTokenServices {

    private RestOperations restTemplate;
    private String checkTokenEndpointUrl;
    private String userInfoEndpointUrl;
    private String clientId;
    private String clientSecret;
    private AccessTokenConverter tokenConverter = new GoogleAccessTokenConverter();

    private static Logger LOG = LoggerFactory.getLogger(GoogleTokenServices.class);

    public GoogleTokenServices() {
        restTemplate = new RestTemplate();
        ((RestTemplate) restTemplate).setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            // Ignore 400
            public void handleError(ClientHttpResponse response) throws IOException {
                if (response.getRawStatusCode() != 400) {
                    super.handleError(response);
                }
            }
        });
    }

    public void setRestTemplate(RestOperations restTemplate) {
        this.restTemplate = restTemplate;
    }

    public void setCheckTokenEndpointUrl(String checkTokenEndpointUrl) {
        this.checkTokenEndpointUrl = checkTokenEndpointUrl;
    }

    public void setUserInfoEndpointUrl(String userInfoEndpointUrl) {
        this.userInfoEndpointUrl = userInfoEndpointUrl;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public void setClientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
    }

    public void setAccessTokenConverter(AccessTokenConverter accessTokenConverter) {
        this.tokenConverter = accessTokenConverter;
    }

    @Override
    public OAuth2Authentication loadAuthentication(String accessToken) throws AuthenticationException, InvalidTokenException {
        Map<String, Object> response = transformToStandardValues(checkToken(accessToken));
        Assert.state(response.containsKey("client_id"), "Client id must be present in response from auth server");
        if (response.containsKey("error")) {
            logger.debug("check_token returned error: " + response.get("error"));
            throw new InvalidTokenException(accessToken);
        }

        Map<String, Object> userInfoResponse = readUser(accessToken);
        if (userInfoResponse.containsKey("error")) {
            logger.debug("read_user returned error: " + response.get("error"));
            throw new InvalidTokenException(accessToken);
        }

        response.putAll(userInfoResponse);
        LOG.info("Google authentication response: " + response);
        return tokenConverter.extractAuthentication(response);
    }

    private Map<String, Object> checkToken(String accessToken) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("token", accessToken);
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
        String accessTokenUrl = new StringBuilder(checkTokenEndpointUrl).append("?access_token=").append(accessToken).toString();
        return postForMap(accessTokenUrl, formData, headers);
    }

    private Map<String, Object> readUser(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", getAuthorizationHeader(clientId, clientSecret));
        String accessTokenUrl = new StringBuilder(userInfoEndpointUrl).append("?access_token=").append(accessToken).toString();
        ParameterizedTypeReference<Map<String, Object>> map = new ParameterizedTypeReference<Map<String, Object>>() {};
        return restTemplate.exchange(accessTokenUrl, HttpMethod.GET, new HttpEntity<>(headers), map).getBody();
    }

    private Map<String, Object> transformToStandardValues(Map<String, Object> map) {
        map.put("client_id", map.get("issued_to")); // Google sends 'client_id' as 'issued_to'
        map.put("user_name", map.get("user_id")); // Google sends 'user_name' as 'user_id'
        return map;
    }

    private String getAuthorizationHeader(String clientId, String clientSecret) {
        String creds = String.format("%s:%s", clientId, clientSecret);
        try {
            return "Basic " + new String(Base64.encode(creds.getBytes("UTF-8")));
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Could not convert String");
        }
    }

    private Map<String, Object> postForMap(String path, MultiValueMap<String, String> formData, HttpHeaders headers) {
        if (headers.getContentType() == null) {
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        }
        ParameterizedTypeReference<Map<String, Object>> map = new ParameterizedTypeReference<Map<String, Object>>() {};
        return restTemplate.exchange(path, HttpMethod.POST, new HttpEntity<>(formData, headers), map).getBody();
    }
}
