package org.bahmni.module.bahmnicore.client;

import org.bahmni.webclients.HttpRequestDetails;
import org.junit.Before;
import org.junit.Test;

import java.net.URI;

import org.bahmni.module.bahmnicore.exception.OdooApiException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BahmniOdooSessionManagerTest {

    private static final String SESSION_COOKIE_VALUE = "session_id=abc123xyz";
    private static final URI TEST_URI = URI.create("http://odoo:8069/api/test");

    private BahmniOdooSessionManager sessionManager;

    @Before
    public void setUp() {
        sessionManager = new BahmniOdooSessionManager();
    }

    @Test
    public void getSessionCookie_shouldReturnCachedCookieOnSecondCall() {
        // Simulate a cached cookie by setting it via the authenticate flow
        // Use reflection or direct field access for test setup
        setSessionCookieViaReflection(SESSION_COOKIE_VALUE);

        String firstCall = sessionManager.getSessionCookie();
        String secondCall = sessionManager.getSessionCookie();

        assertEquals(SESSION_COOKIE_VALUE, firstCall);
        assertEquals(firstCall, secondCall);
    }

    @Test
    public void clearSessionCache_shouldClearTheCachedCookie() {
        setSessionCookieViaReflection(SESSION_COOKIE_VALUE);

        assertEquals(SESSION_COOKIE_VALUE, sessionManager.getSessionCookie());

        sessionManager.clearSessionCache();

        // After clearing, the next call should try to authenticate again
        // Since there's no real Odoo server, we verify the cache was cleared
        // by checking that the internal state is null
        assertNull(getCachedCookieViaReflection());
    }

    @Test
    public void getRequestDetails_shouldReturnHttpRequestDetailsWithSessionCookie() {
        setSessionCookieViaReflection(SESSION_COOKIE_VALUE);

        HttpRequestDetails details = sessionManager.getRequestDetails(TEST_URI);

        assertNotNull(details);
        assertEquals(TEST_URI, details.getUri());
        assertNotNull(details.getClientCookies());
        assertEquals("abc123xyz", details.getClientCookies().get("session_id"));
    }

    @Test
    public void getRequestDetails_shouldReturnCachedRequestDetailsOnSubsequentCalls() {
        setSessionCookieViaReflection(SESSION_COOKIE_VALUE);

        HttpRequestDetails firstDetails = sessionManager.getRequestDetails(TEST_URI);
        URI anotherUri = URI.create("http://odoo:8069/api/other");
        HttpRequestDetails secondDetails = sessionManager.getRequestDetails(anotherUri);

        assertNotNull(firstDetails);
        assertNotNull(secondDetails);
        // Second call should reuse cached auth but with new URI
        assertEquals(anotherUri, secondDetails.getUri());
        assertEquals("abc123xyz", secondDetails.getClientCookies().get("session_id"));
    }

    @Test
    public void refreshRequestDetails_shouldClearPreviousCacheAndAttemptReAuthentication() {
        setSessionCookieViaReflection(SESSION_COOKIE_VALUE);

        // Build initial request details
        sessionManager.getRequestDetails(TEST_URI);

        // refreshRequestDetails clears cache internally and tries to re-authenticate.
        // Without a real Odoo server, authenticate() will throw OdooApiException.
        try {
            sessionManager.refreshRequestDetails(TEST_URI);
            fail("Expected OdooApiException since no Odoo server is available");
        } catch (OdooApiException ex) {
            // Verify cache was cleared by refreshRequestDetails
            assertNull(getCachedCookieViaReflection());
            assertNotNull(ex.getMessage());
        }
    }

    @Test
    public void getRequestDetails_shouldBuildNewDetailsWhenCookieDoesNotContainEquals() {
        // Cookie without "=" should result in empty ClientCookies
        setSessionCookieViaReflection("invalidcookieformat");

        HttpRequestDetails details = sessionManager.getRequestDetails(TEST_URI);

        assertNotNull(details);
        assertEquals(TEST_URI, details.getUri());
        assertNotNull(details.getClientCookies());
    }

    @Test
    public void getRequestDetails_shouldBuildNewDetailsWhenCookieIsNull() {
        // Set cookie to null, but also set previousSuccessfulRequest to non-null
        // so getRequestDetails takes the cached path
        setSessionCookieViaReflection("session_id=initial");
        sessionManager.getRequestDetails(TEST_URI);

        // Now clear only the cookie cache but keep previousSuccessfulRequest
        setSessionCookieViaReflection(null);

        // This should still use previousSuccessfulRequest.createNewWith(uri)
        URI newUri = URI.create("http://odoo:8069/api/other");
        HttpRequestDetails details = sessionManager.getRequestDetails(newUri);

        assertNotNull(details);
        assertEquals(newUri, details.getUri());
    }

    @Test
    public void getSessionCookie_shouldThrowOdooApiExceptionWhenAuthenticateFails() {
        // With no cached cookie and no real Odoo server, authenticate() will throw
        try {
            sessionManager.getSessionCookie();
            fail("Expected OdooApiException since no Odoo server is available");
        } catch (OdooApiException ex) {
            assertNotNull(ex.getMessage());
            assertTrue(ex.getMessage().contains("Authentication failed"));
        }
    }

    @Test
    public void clearSessionCache_shouldAllowRecachingAfterClear() {
        setSessionCookieViaReflection("session_id=original");
        assertEquals("session_id=original", sessionManager.getSessionCookie());

        sessionManager.clearSessionCache();
        assertNull(getCachedCookieViaReflection());

        // Re-set via reflection to simulate re-authentication
        setSessionCookieViaReflection("session_id=refreshed");
        assertEquals("session_id=refreshed", sessionManager.getSessionCookie());
    }

    // ---- helpers ----

    private void setSessionCookieViaReflection(String cookie) {
        try {
            java.lang.reflect.Field field = BahmniOdooSessionManager.class.getDeclaredField("cachedSessionCookie");
            field.setAccessible(true);
            field.set(sessionManager, cookie);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set session cookie via reflection", e);
        }
    }

    private String getCachedCookieViaReflection() {
        try {
            java.lang.reflect.Field field = BahmniOdooSessionManager.class.getDeclaredField("cachedSessionCookie");
            field.setAccessible(true);
            return (String) field.get(sessionManager);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get session cookie via reflection", e);
        }
    }
}
