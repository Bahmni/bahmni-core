package org.bahmni.module.bahmnicore.web.v1_0.client;

import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;

@Component
public class TemplateServiceClient {

    private static final String URL_KEY = "template.service.url";
    private static final String CONNECT_TIMEOUT_KEY = "template.service.connectTimeoutInMilliseconds";
    private static final String READ_TIMEOUT_KEY = "template.service.readTimeoutInMilliseconds";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public TemplateServiceClient() {
        this.baseUrl = BahmniCoreProperties.getProperty(URL_KEY);
        this.restTemplate = buildRestTemplate();
    }

    TemplateServiceClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<String> forward(HttpMethod method, String path,
                                          String queryString, HttpHeaders headers,
                                          String body) {
        String url = baseUrl + path + (queryString != null ? "?" + queryString : "");
        try {
            return restTemplate.exchange(url, method, new HttpEntity<>(body, headers), String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(getIntProperty(CONNECT_TIMEOUT_KEY, DEFAULT_CONNECT_TIMEOUT));
        factory.setReadTimeout(getIntProperty(READ_TIMEOUT_KEY, DEFAULT_READ_TIMEOUT));
        return new RestTemplate(factory);
    }

    private int getIntProperty(String key, int defaultValue) {
        String value = BahmniCoreProperties.getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }
}
