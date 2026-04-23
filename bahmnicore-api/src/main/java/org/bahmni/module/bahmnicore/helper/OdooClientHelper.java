package org.bahmni.module.bahmnicore.helper;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public class OdooClientHelper {

    private static final Logger logger = LogManager.getLogger(OdooClientHelper.class);

    private OdooClientHelper() {
        // Utility class — prevent instantiation
    }

    public static HttpEntity<String> createAuthenticationRequest(
            String database, String login, String password) {
        String jsonBody = String.format(
                "{\"params\":{\"db\":\"%s\",\"login\":\"%s\",\"password\":\"%s\"}}",
                database, login, password);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        return new HttpEntity<>(jsonBody, headers);
    }

    public static HttpHeaders createAuthenticatedHeaders(String sessionCookie) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, sessionCookie);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

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
