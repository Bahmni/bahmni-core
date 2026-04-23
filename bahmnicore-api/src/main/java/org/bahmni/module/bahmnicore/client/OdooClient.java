package org.bahmni.module.bahmnicore.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.helper.OdooClientHelper;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP client for communicating with Odoo.
 * Supports authenticated GET requests with automatic retry on 401/403.
 */
public class OdooClient {

    private static final Logger logger = LogManager.getLogger(OdooClient.class);

    private final RestTemplate restTemplate;
    private final OdooSessionManager sessionManager;

    public OdooClient(RestTemplate restTemplate, OdooSessionManager sessionManager) {
        this.restTemplate = restTemplate;
        this.sessionManager = sessionManager;
    }

    public <T> T get(String url, Class<T> returnType) {
        String sessionCookie = sessionManager.getSessionCookie();
        HttpHeaders headers = OdooClientHelper.createAuthenticatedHeaders(sessionCookie);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<T> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, returnType);
        return response.getBody();
    }

    public <T> T getWithAuthRetry(String url, Class<T> returnType) {
        try {
            return get(url, returnType);
        } catch (HttpClientErrorException ex) {
            if (isAuthenticationError(ex)) {
                logger.warn("Authentication error ({}), clearing session cache and retrying", ex.getStatusCode());
                sessionManager.clearSessionCache();
                return get(url, returnType);
            }
            throw ex;
        }
    }

    private boolean isAuthenticationError(HttpClientErrorException ex) {
        return ex.getStatusCode() == HttpStatus.UNAUTHORIZED
                || ex.getStatusCode() == HttpStatus.FORBIDDEN;
    }
}
