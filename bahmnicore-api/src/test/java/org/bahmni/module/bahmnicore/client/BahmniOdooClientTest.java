package org.bahmni.module.bahmnicore.client;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniOdooClientTest {

    private static final String TEST_URL = "http://odoo:8069/api/get-available-stocks?productUuid=abc";
    private static final String SESSION_COOKIE = "session_id=test-session-123";
    private static final String RESPONSE_BODY = "{\"count\":2,\"data\":[]}";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private BahmniOdooSessionManager sessionManager;

    @InjectMocks
    private BahmniOdooClient bahmniOdooClient;

    @Before
    public void setUp() {
        when(sessionManager.getSessionCookie()).thenReturn(SESSION_COOKIE);
    }

    @Test
    public void get_shouldCallRestTemplateWithAuthenticatedHeadersAndReturnBody() {
        ResponseEntity<String> mockResponse = new ResponseEntity<>(RESPONSE_BODY, HttpStatus.OK);
        when(restTemplate.exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenReturn(mockResponse);

        String result = bahmniOdooClient.get(TEST_URL);

        assertEquals(RESPONSE_BODY, result);
        verify(sessionManager, times(1)).getSessionCookie();
        verify(sessionManager, never()).clearSessionCache();
        verify(restTemplate, times(1)).exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void get_shouldClearSessionCacheAndRetryOn401Unauthorized() {
        HttpClientErrorException unauthorizedException =
                new HttpClientErrorException(HttpStatus.UNAUTHORIZED);
        ResponseEntity<String> retryResponse = new ResponseEntity<>(RESPONSE_BODY, HttpStatus.OK);

        when(restTemplate.exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(unauthorizedException)
                .thenReturn(retryResponse);

        String result = bahmniOdooClient.get(TEST_URL);

        assertEquals(RESPONSE_BODY, result);
        verify(sessionManager, times(1)).clearSessionCache();
        verify(restTemplate, times(2)).exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void get_shouldClearSessionCacheAndRetryOn403Forbidden() {
        HttpClientErrorException forbiddenException =
                new HttpClientErrorException(HttpStatus.FORBIDDEN);
        ResponseEntity<String> retryResponse = new ResponseEntity<>(RESPONSE_BODY, HttpStatus.OK);

        when(restTemplate.exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(forbiddenException)
                .thenReturn(retryResponse);

        String result = bahmniOdooClient.get(TEST_URL);

        assertEquals(RESPONSE_BODY, result);
        verify(sessionManager, times(1)).clearSessionCache();
        verify(restTemplate, times(2)).exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void get_shouldRethrowNonAuthErrorsWithoutRetrying() {
        HttpClientErrorException notFoundException =
                new HttpClientErrorException(HttpStatus.NOT_FOUND);

        when(restTemplate.exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(notFoundException);

        try {
            bahmniOdooClient.get(TEST_URL);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        }

        verify(sessionManager, never()).clearSessionCache();
        verify(restTemplate, times(1)).exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }

    @Test
    public void get_shouldRethrowInternalServerErrorWithoutRetrying() {
        HttpClientErrorException serverException =
                new HttpClientErrorException(HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class)))
                .thenThrow(serverException);

        try {
            bahmniOdooClient.get(TEST_URL);
            fail("Expected HttpClientErrorException to be thrown");
        } catch (HttpClientErrorException ex) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, ex.getStatusCode());
        }

        verify(sessionManager, never()).clearSessionCache();
        verify(restTemplate, times(1)).exchange(eq(TEST_URL), eq(HttpMethod.GET), any(HttpEntity.class), eq(String.class));
    }
}
