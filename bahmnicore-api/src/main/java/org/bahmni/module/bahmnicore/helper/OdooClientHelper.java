package org.bahmni.module.bahmnicore.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;

public class OdooClientHelper {

    private static final Logger logger = LogManager.getLogger(OdooClientHelper.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static final String SESSION_COOKIE_NAME = "session_id";

    public static HttpEntity<String> createAuthenticationRequest(
            String database, String login, String password) {
        try {
            ObjectNode params = objectMapper.createObjectNode();
            params.put("db", database);
            params.put("login", login);
            params.put("password", password);

            ObjectNode root = objectMapper.createObjectNode();
            root.set("params", params);

            String jsonBody = objectMapper.writeValueAsString(root);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            return new HttpEntity<>(jsonBody, headers);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to build authentication request body", e);
        }
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
        return cookies.stream()
                .filter(c -> c.startsWith(SESSION_COOKIE_NAME + "="))
                .map(c -> c.contains(";") ? c.substring(0, c.indexOf(";")) : c)
                .findFirst()
                .orElseGet(() -> {
                    logger.warn("session_id cookie not found among authentication response cookies: {}", cookies);
                    return null;
                });
    }
}
