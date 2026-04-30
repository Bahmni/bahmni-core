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

public class BahmniOdooClient {

    private static final Logger logger = LogManager.getLogger(BahmniOdooClient.class);

    private final RestTemplate restTemplate;
    private final BahmniOdooSessionManager sessionManager;

    public BahmniOdooClient(RestTemplate restTemplate, BahmniOdooSessionManager sessionManager) {
        this.restTemplate = restTemplate;
        this.sessionManager = sessionManager;
    }

    public String get(String url) {
        try {
            return executeGet(url);
        } catch (HttpClientErrorException ex) {
            if (isAuthenticationError(ex)) {
                logger.warn("Authentication error ({}), clearing session cache and retrying", ex.getStatusCode());
                sessionManager.clearSessionCache();
                return executeGet(url);
            }
            throw ex;
        }
    }

    private String executeGet(String url) {
        String sessionCookie = sessionManager.getSessionCookie();
        HttpHeaders headers = OdooClientHelper.createAuthenticatedHeaders(sessionCookie);
        HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, requestEntity, String.class);
        return response.getBody();
    }

    private boolean isAuthenticationError(HttpClientErrorException ex) {
        return ex.getStatusCode() == HttpStatus.UNAUTHORIZED
                || ex.getStatusCode() == HttpStatus.FORBIDDEN;
    }
}
