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
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TemplateServiceClientTest {

    @Mock
    private RestTemplate restTemplate;

    private TemplateServiceClient client;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        client = new TemplateServiceClient(restTemplate, "http://template-service:8080");
    }

    @Test
    public void getTemplatesCallsCorrectUrl() {
        ResponseEntity<String> expected = new ResponseEntity<>("[{\"id\":\"1\"}]", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://template-service:8080/api/templates"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        ResponseEntity<String> result = client.getTemplates(new HttpHeaders());

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("[{\"id\":\"1\"}]", result.getBody());
        verify(restTemplate).exchange(eq("http://template-service:8080/api/templates"), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void getTemplatesPassesThroughDownstream4xxStatus() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.NOT_FOUND));

        ResponseEntity<String> result = client.getTemplates(new HttpHeaders());

        assertEquals(HttpStatus.NOT_FOUND, result.getStatusCode());
    }

    @Test
    public void getTemplatesPassesThroughDownstream5xxStatus() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = client.getTemplates(new HttpHeaders());

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void renderCallsCorrectUrl() {
        HttpHeaders headers = new HttpHeaders();
        ResponseEntity<String> expected = new ResponseEntity<>("rendered", HttpStatus.OK);
        when(restTemplate.exchange(eq("http://template-service:8080/api/render"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class))).thenReturn(expected);

        ResponseEntity<String> result = client.render(headers, "{\"id\":\"1\"}");

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("rendered", result.getBody());
        verify(restTemplate).exchange(eq("http://template-service:8080/api/render"), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void renderPassesThroughDownstream4xxStatus() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<String> result = client.render(new HttpHeaders(), "{}");

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    public void renderPassesThroughDownstream5xxStatus() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<String> result = client.render(new HttpHeaders(), "{}");

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    @Test
    public void parseIntPropertyReturnsDefaultWhenValueIsNull() {
        assertEquals(5000, TemplateServiceClient.parseIntProperty(null, 5000));
    }

    @Test
    public void parseIntPropertyParsesValidIntAndTrimsWhitespace() {
        assertEquals(3000, TemplateServiceClient.parseIntProperty("  3000  ", 5000));
    }

    @Test
    public void parseIntPropertyReturnsDefaultForNonNumericValue() {
        assertEquals(5000, TemplateServiceClient.parseIntProperty("not-a-number", 5000));
    }

    @Test
    public void getTemplatesReturnsBadGatewayWhenServiceUnreachable() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.GET),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ResponseEntity<String> result = client.getTemplates(new HttpHeaders());

        assertEquals(HttpStatus.BAD_GATEWAY, result.getStatusCode());
        assertEquals(true, result.getBody().contains("unavailable"));
    }

    @Test
    public void renderReturnsBadGatewayWhenServiceUnreachable() {
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenThrow(new ResourceAccessException("Connection refused"));

        ResponseEntity<String> result = client.render(new HttpHeaders(), "{}");

        assertEquals(HttpStatus.BAD_GATEWAY, result.getStatusCode());
        assertEquals(true, result.getBody().contains("unavailable"));
    }

    @Test
    public void renderSendsBodyToService() {
        String body = "{\"templateId\":\"abc\"}";
        when(restTemplate.exchange(any(String.class), eq(HttpMethod.POST),
                any(HttpEntity.class), eq(String.class)))
                .thenReturn(new ResponseEntity<>("output", HttpStatus.OK));

        client.render(new HttpHeaders(), body);

        verify(restTemplate).exchange(eq("http://template-service:8080/api/render"),
                eq(HttpMethod.POST), any(HttpEntity.class), eq(String.class));
    }
}
