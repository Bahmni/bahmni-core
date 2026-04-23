package org.bahmni.module.bahmnicore.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

/**
 * Static utility for building Odoo authentication requests,
 * creating authenticated headers, and extracting session cookies.
 */
public class OdooClientHelper {

    private static final Logger logger = LogManager.getLogger(OdooClientHelper.class);

    private OdooClientHelper() {
        // Utility class — prevent instantiation
    }

    /**
     * Builds the Odoo authentication request body as a JSON string:
     * { "params": { "db": "...", "login": "...", "password": "..." } }
     *
     * Uses a String body instead of Map to avoid Jackson serialization issues
     * in the OpenMRS module classloader context.
     */
    public static HttpEntity<String> createAuthenticationRequest(
            String database, String login, String password) {
        String jsonBody = String.format(
                "{\"params\":{\"db\":\"%s\",\"login\":\"%s\",\"password\":\"%s\"}}",
                database, login, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(jsonBody, headers);
    }

    /**
     * Creates HTTP headers with the session cookie and JSON content type.
     */
    public static HttpHeaders createAuthenticatedHeaders(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    /**
     * Extracts the session cookie from the Set-Cookie header values.
     * Returns the first cookie value (everything before the first ';').
     */
    public static String extractSessionCookie(List<String> cookies) {
        if (cookies == null || cookies.isEmpty()) {
            logger.warn("No cookies found in authentication response");
            return null;
        }
        String cookie = cookies.get(0);
        if (cookie.contains(";")) {
            cookie = cookie.substring(0, cookie.indexOf(";"));
        }
        return cookie;
    }
}
