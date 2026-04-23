package org.bahmni.module.bahmnicore.client;

import org.bahmni.module.bahmnicore.exception.OdooApiException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniOdooSessionManagerTest {

    private static final String SESSION_COOKIE_VALUE = "session_id=abc123xyz";

    @Mock
    private RestTemplate restTemplate;

    private BahmniOdooSessionManager sessionManager;

    @Before
    public void setUp() {
        sessionManager = new BahmniOdooSessionManager(restTemplate);
    }

    @Test
    public void getSessionCookie_shouldAuthenticateWithOdooAndReturnSessionCookie() {
        ResponseEntity<String> authResponse = buildAuthResponse(SESSION_COOKIE_VALUE + "; Path=/; HttpOnly");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(authResponse);

        String cookie = sessionManager.getSessionCookie();

        assertNotNull(cookie);
        assertEquals(SESSION_COOKIE_VALUE, cookie);
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    public void getSessionCookie_shouldReturnCachedCookieWithoutReauthenticatingOnSecondCall() {
        ResponseEntity<String> authResponse = buildAuthResponse(SESSION_COOKIE_VALUE + "; Path=/");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(authResponse);

        String firstCall = sessionManager.getSessionCookie();
        String secondCall = sessionManager.getSessionCookie();

        assertEquals(firstCall, secondCall);
        // RestTemplate.exchange() should only be called once despite two getSessionCookie() calls
        verify(restTemplate, times(1)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    public void clearSessionCache_shouldForceReauthenticationOnNextGetSessionCookieCall() {
        ResponseEntity<String> authResponse = buildAuthResponse(SESSION_COOKIE_VALUE + "; Path=/");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenReturn(authResponse);

        sessionManager.getSessionCookie();
        sessionManager.clearSessionCache();
        sessionManager.getSessionCookie();

        // exchange() should be called twice: once before clear, once after clear
        verify(restTemplate, times(2)).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
    }

    @Test
    public void getSessionCookie_shouldThrowOdooApiExceptionWhenAuthenticationFails() {
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class)))
                .thenThrow(new RestClientException("Connection refused"));

        try {
            sessionManager.getSessionCookie();
            fail("Expected OdooApiException to be thrown");
        } catch (OdooApiException ex) {
            assertNotNull(ex.getMessage());
            assertNotNull(ex.getCause());
            assertEquals(RestClientException.class, ex.getCause().getClass());
        }
    }

    @Test
    public void clearSessionCache_shouldBeIdempotentWhenCalledMultipleTimes() {
        // Clearing an already-empty cache should not throw
        sessionManager.clearSessionCache();
        sessionManager.clearSessionCache();
        // No exception expected
    }

    // ---- helpers ----

    private ResponseEntity<String> buildAuthResponse(String setCookieValue) {
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<>();
        headers.put(HttpHeaders.SET_COOKIE, Collections.singletonList(setCookieValue));
        return new ResponseEntity<>("{\"result\":{}}", new HttpHeaders(headers), HttpStatus.OK);
    }
}
