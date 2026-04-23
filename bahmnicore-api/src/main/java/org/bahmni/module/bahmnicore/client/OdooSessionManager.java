package org.bahmni.module.bahmnicore.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.bahmni.module.bahmnicore.helper.OdooClientHelper;
import org.bahmni.module.bahmnicore.properties.OdooConfigProperties;
import org.bahmni.module.bahmnicore.util.OdooUrlBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

public class OdooSessionManager {

    private static final Logger logger = LogManager.getLogger(OdooSessionManager.class);

    private final RestTemplate restTemplate;
    private volatile String cachedSessionCookie;

    public OdooSessionManager(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String getSessionCookie() {
        String cookie = cachedSessionCookie;
        if (cookie != null) {
            return cookie;
        }
        logger.info("Authenticating with Odoo to obtain session cookie");
        cookie = authenticate();
        cachedSessionCookie = cookie;
        return cookie;
    }

    public void clearSessionCache() {
        logger.info("Clearing cached Odoo session cookie");
        cachedSessionCookie = null;
    }

    private String authenticate() {
        try {
            String database = OdooConfigProperties.getProperty("odoo.database");
            String username = OdooConfigProperties.getProperty("odoo.username");
            String password = OdooConfigProperties.getProperty("odoo.password");

            String authUrl = OdooUrlBuilder.buildAuthenticationUrl();
            HttpEntity<String> authRequest =
                    OdooClientHelper.createAuthenticationRequest(database, username, password);

            System.out.println("Authenticating with Odoo: " + authUrl + "request: " + authRequest);

            ResponseEntity<String> authResponse =
                    restTemplate.exchange(authUrl, HttpMethod.POST, authRequest, String.class);

            List<String> cookies = authResponse.getHeaders().get(HttpHeaders.SET_COOKIE);
            String sessionCookie = OdooClientHelper.extractSessionCookie(cookies);

            logger.info("Successfully authenticated with Odoo");
            return sessionCookie;
        } catch (RestClientException ex) {
            throw new OdooApiException("Authentication failed: " + ex.getMessage(), ex);
        }
    }
}
