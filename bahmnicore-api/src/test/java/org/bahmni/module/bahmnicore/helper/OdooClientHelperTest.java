package org.bahmni.module.bahmnicore.helper;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
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

    @Test
    public void extractSessionCookie_shouldReturnNullWhenNoSessionIdCookiePresent() {
        List<String> cookies = Arrays.asList(
                "other_cookie=somevalue; Path=/",
                "csrf_token=xyz789; HttpOnly"
        );

        String result = OdooClientHelper.extractSessionCookie(cookies);

        assertNull(result);
    }

    @Test
    public void createAuthenticationRequestBody_shouldHandleNullInputs() {
        String body = OdooClientHelper.createAuthenticationRequestBody(null, null, null);

        assertNotNull(body);
        assertTrue(body.contains("\"params\""));
    }

    @Test
    public void createAuthenticationRequestBody_shouldHandleEmptyInputs() {
        String body = OdooClientHelper.createAuthenticationRequestBody("", "", "");

        assertNotNull(body);
        assertTrue(body.contains("\"db\":\"\""));
        assertTrue(body.contains("\"login\":\"\""));
        assertTrue(body.contains("\"password\":\"\""));
    }

    @Test
    public void extractSessionCookie_shouldHandleSingleNonSessionCookie() {
        List<String> cookies = Collections.singletonList("random_cookie=value");

        String result = OdooClientHelper.extractSessionCookie(cookies);

        assertNull(result);
    }

    @Test(expected = IllegalStateException.class)
    public void createAuthenticationRequestBody_shouldThrowIllegalStateExceptionWhenSerializationFails() throws Exception {
        ObjectMapper brokenMapper = Mockito.mock(ObjectMapper.class);
        Mockito.when(brokenMapper.createObjectNode()).thenThrow(new RuntimeException("Simulated serialization failure"));

        Field field = OdooClientHelper.class.getDeclaredField("objectMapper");
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        ObjectMapper original = (ObjectMapper) field.get(null);
        try {
            field.set(null, brokenMapper);
            OdooClientHelper.createAuthenticationRequestBody("db", "login", "pass");
        } finally {
            field.set(null, original);
            modifiersField.setInt(field, field.getModifiers() | Modifier.FINAL);
        }
    }
}
