package org.bahmni.module.bahmnicore.client;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;

import java.net.URI;
import java.util.concurrent.TimeUnit;

public class BahmniOdooClient {

    private static final Logger logger = LogManager.getLogger(BahmniOdooClient.class);

    private HttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;
    private final BahmniOdooSessionManager sessionManager;


    public BahmniOdooClient(BahmniOdooSessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.connectionManager = new PoolingHttpClientConnectionManager();
        ConnectionDetails connectionDetails = new ConnectionDetails(
                "", "", "", 20000, 30000, connectionManager);
        this.httpClient = new HttpClient(connectionDetails, new BahmniOdooSessionManager());
    }

    BahmniOdooClient(HttpClient httpClient, BahmniOdooSessionManager sessionManager) {
        this.httpClient = httpClient;
        this.sessionManager = sessionManager;
        this.connectionManager = null;
    }

    public String get(String url) {
        try {
            return httpClient.get(URI.create(url));
        } catch (Exception e) {
            logger.warn("Odoo request failed, resetting connection pool ", e);
            resetConnectionPool();
            throw e;
        }
    }

    private synchronized void resetConnectionPool() {
        if (connectionManager != null) {
            connectionManager.shutdown();
        }
        this.connectionManager = new PoolingHttpClientConnectionManager();
        ConnectionDetails cd = new ConnectionDetails("", "", "", 20000, 30000, connectionManager);
        this.httpClient = new HttpClient(cd, sessionManager);
    }
}
