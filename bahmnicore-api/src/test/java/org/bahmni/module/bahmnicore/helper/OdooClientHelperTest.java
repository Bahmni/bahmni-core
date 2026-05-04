package org.bahmni.module.bahmnicore.helper;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OdooClientHelperTest {

    @Test
    public void createAuthenticationRequestBody_shouldBuildCorrectJsonBodyWithCredentials() {
        String body = OdooClientHelper.createAuthenticationRequestBody("mydb", "admin", "secret");

        assertNotNull(body);
        assertTrue(body.contains("\"db\":\"mydb\""));
        assertTrue(body.contains("\"login\":\"admin\""));
        assertTrue(body.contains("\"password\":\"secret\""));
        assertTrue(body.contains("\"params\""));
    }

    @Test
    public void createAuthenticationRequestBody_shouldReturnValidJsonString() {
        String body = OdooClientHelper.createAuthenticationRequestBody("mydb", "admin", "secret");

        assertNotNull(body);
        assertTrue(body.startsWith("{"));
        assertTrue(body.endsWith("}"));
    }

    @Test
    public void extractSessionCookie_shouldReturnCookieValueBeforeSemicolon() {
        List<String> cookies = Collections.singletonList("session_id=abc123; Path=/; HttpOnly");

        String result = OdooClientHelper.extractSessionCookie(cookies);

        assertEquals("session_id=abc123", result);
    }

    @Test
    public void extractSessionCookie_shouldReturnFullCookieWhenNoSemicolonPresent() {
        List<String> cookies = Collections.singletonList("session_id=abc123");

        String result = OdooClientHelper.extractSessionCookie(cookies);

        assertEquals("session_id=abc123", result);
    }

    @Test
    public void extractSessionCookie_shouldUseFirstCookieFromList() {
        List<String> cookies = Arrays.asList(
                "session_id=first-session; Path=/",
                "session_id=second-session; Path=/"
        );

        String result = OdooClientHelper.extractSessionCookie(cookies);

        assertEquals("session_id=first-session", result);
    }

    @Test
    public void extractSessionCookie_shouldReturnNullWhenCookiesListIsNull() {
        String result = OdooClientHelper.extractSessionCookie(null);

        assertNull(result);
    }

    @Test
    public void extractSessionCookie_shouldReturnNullWhenCookiesListIsEmpty() {
        String result = OdooClientHelper.extractSessionCookie(Collections.emptyList());

        assertNull(result);
    }
}
