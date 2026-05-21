package org.bahmni.module.bahmnicore.web.v1_0.client;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class TemplateServiceClient {

    private static final Log log = LogFactory.getLog(TemplateServiceClient.class);

    private static final String URL_KEY = "template.service.url";
    private static final String CONNECT_TIMEOUT_KEY = "template.service.connectTimeoutInMilliseconds";
    private static final String READ_TIMEOUT_KEY = "template.service.readTimeoutInMilliseconds";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final String TEMPLATES_PATH = "/api/templates";
    private static final String RENDER_PATH = "/api/render";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public TemplateServiceClient() {
        this.baseUrl = BahmniCoreProperties.getProperty(URL_KEY);
        this.restTemplate = buildRestTemplate();
    }

    TemplateServiceClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<String> getTemplates(HttpHeaders headers) {
        try {
            return restTemplate.exchange(baseUrl + TEMPLATES_PATH, HttpMethod.GET,
                    new HttpEntity<>(headers), String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Template service unreachable at " + baseUrl, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Template service is unavailable: " + e.getMessage());
        }
    }

    public ResponseEntity<String> render(HttpHeaders headers, String body) {
        try {
            return restTemplate.exchange(baseUrl + RENDER_PATH, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Template service unreachable at " + baseUrl, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Template service is unavailable: " + e.getMessage());
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(parseIntProperty(BahmniCoreProperties.getProperty(CONNECT_TIMEOUT_KEY), DEFAULT_CONNECT_TIMEOUT));
        factory.setReadTimeout(parseIntProperty(BahmniCoreProperties.getProperty(READ_TIMEOUT_KEY), DEFAULT_READ_TIMEOUT));
        return new RestTemplate(factory);
    }

    static int parseIntProperty(String value, int defaultValue) {
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
