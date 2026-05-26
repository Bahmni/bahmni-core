package org.bahmni.module.bahmnicore.client;

import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.bahmni.module.bahmnicore.helper.OdooClientHelper;
import org.bahmni.module.bahmnicore.properties.OdooConfigProperties;
import org.bahmni.module.bahmnicore.util.OdooUrlBuilder;
import org.bahmni.webclients.Authenticator;
import org.bahmni.webclients.ClientCookies;
import org.bahmni.webclients.HttpHeaders;
import org.bahmni.webclients.HttpRequestDetails;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class BahmniOdooSessionManager implements Authenticator {

    private static final Logger logger = LogManager.getLogger(BahmniOdooSessionManager.class);

    private volatile String cachedSessionCookie;
    private HttpRequestDetails previousSuccessfulRequest;

    @Override
    public HttpRequestDetails getRequestDetails(URI uri) {
        if (previousSuccessfulRequest != null) {
            return previousSuccessfulRequest.createNewWith(uri);
        }
        return buildRequestDetails(uri);
    }

    @Override
    public HttpRequestDetails refreshRequestDetails(URI uri) {
        logger.info("Refreshing Odoo authentication for retry");
        clearSessionCache();
        previousSuccessfulRequest = null;
        return buildRequestDetails(uri);
    }

    private HttpRequestDetails buildRequestDetails(URI uri) {
        String cookie = getSessionCookie();
        ClientCookies cookies = new ClientCookies();
        if (cookie != null && cookie.contains("=")) {
            String[] parts = cookie.split("=", 2);
            cookies.put(parts[0], parts[1]);
        }
        previousSuccessfulRequest = new HttpRequestDetails(uri, cookies, new HttpHeaders());
        return previousSuccessfulRequest;
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
            String jsonBody = OdooClientHelper.createAuthenticationRequestBody(database, username, password);

            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost httpPost = new HttpPost(authUrl);
                httpPost.setHeader("Content-Type", "application/json");
                httpPost.setEntity(new StringEntity(jsonBody));

                try (CloseableHttpResponse response = httpClient.execute(httpPost)) {
                    List<String> cookies = Arrays.stream(response.getHeaders("Set-Cookie"))
                            .map(Header::getValue)
                            .collect(Collectors.toList());

                    String sessionCookie = OdooClientHelper.extractSessionCookie(cookies);
                    logger.info("Successfully authenticated with Odoo");
                    return sessionCookie;
                }
            }
        } catch (Exception ex) {
            throw new OdooApiException("Authentication failed: " + ex.getMessage(), ex);
        }
    }
}
