package org.bahmni.module.bahmnicore.client;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;

import java.net.URI;

public class BahmniOdooClient {

    private static final Logger logger = LogManager.getLogger(BahmniOdooClient.class);

    private final HttpClient httpClient;

    public BahmniOdooClient() {
        ConnectionDetails connectionDetails = new ConnectionDetails("", "", "", 20000, 30000);
        this.httpClient = new HttpClient(connectionDetails, new BahmniOdooSessionManager());
    }

    BahmniOdooClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    public String get(String url) {
        logger.debug("Making GET request to Odoo: {}", url);
        return httpClient.get(URI.create(url));
    }
}
