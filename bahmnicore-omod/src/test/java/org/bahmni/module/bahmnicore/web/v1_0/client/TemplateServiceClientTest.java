package org.bahmni.module.bahmnicore.web.v1_0.client;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private TemplateServiceClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        client = new TemplateServiceClient(restTemplate, "http://template-service:8080");
    }

    @Test
    public void forwardBuildsUrlFromBaseUrlAndPath() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-OpenMRS-Session-Id", "test-session");
        ResponseEntity<String> expected = new ResponseEntity<>("body", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://template-service:8080/reports"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        ResponseEntity<String> result = client.forward(HttpMethod.GET, "/reports", null, headers, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("body", result.getBody());
        verify(restTemplate).exchange(eq("http://template-service:8080/reports"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void forwardAppendsQueryStringToUrl() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> expected = new ResponseEntity<>("result", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://template-service:8080/reports?format=pdf"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        client.forward(HttpMethod.GET, "/reports", "format=pdf", headers, null);

        verify(restTemplate).exchange(eq("http://template-service:8080/reports?format=pdf"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void forwardOmitsQueryStringSeparatorWhenQueryStringIsNull() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> expected = new ResponseEntity<>("result", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://template-service:8080/reports"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        client.forward(HttpMethod.GET, "/reports", null, headers, null);

        verify(restTemplate).exchange(eq("http://template-service:8080/reports"),
                eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void forwardPassesThroughDownstream4xxStatus() {
        HttpHeaders headers = new HttpHeaders();
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity<String> result = client.forward(HttpMethod.GET, "/missing", null, headers, null);

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void forwardPassesThroughDownstream5xxStatus() {
        HttpHeaders headers = new HttpHeaders();
        when(restTemplate.exchange(any(String.class), any(HttpMethod.class),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = client.forward(HttpMethod.GET, "/failing", null, headers, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void forwardSendsBodyForPostRequest() {
        HttpHeaders headers = new HttpHeaders();
        String body = "{\"key\":\"value\"}";
        ResponseEntity<String> expected = new ResponseEntity<>("created", HttpStatus.CREATED);
        when(restTemplate.exchange(eq("http://template-service:8080/render"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        ResponseEntity<String> result = client.forward(HttpMethod.POST, "/render", null, headers, body);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals("created", result.getBody());
    }
}
