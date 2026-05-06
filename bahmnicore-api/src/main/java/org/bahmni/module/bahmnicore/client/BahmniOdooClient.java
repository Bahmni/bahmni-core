package org.bahmni.module.bahmnicore.client;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.webclients.ConnectionDetails;
import org.bahmni.webclients.HttpClient;

import java.net.URI;

public class BahmniOdooClient {


    private HttpClient httpClient;
    private PoolingHttpClientConnectionManager connectionManager;

    public BahmniOdooClient(BahmniOdooSessionManager sessionManager) {
        this.connectionManager = new PoolingHttpClientConnectionManager();
        ConnectionDetails connectionDetails = new ConnectionDetails(
                "", "", "", 20000, 30000, connectionManager);
        this.httpClient = new HttpClient(connectionDetails, new BahmniOdooSessionManager());
    }

    BahmniOdooClient(HttpClient httpClient ) {
        this.httpClient = httpClient;
        this.connectionManager = null;
    }

    public String get(String url) {
            return httpClient.get(URI.create(url));
    }


}
