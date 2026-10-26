package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.client.AiServiceClient;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class AiServiceControllerTest {

    @Mock private AiServiceClient client;
    private boolean allowed;
    private boolean authenticated;
    private AiServiceController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        allowed = true;
        authenticated = true;
        controller = new AiServiceController(client) {
            @Override
            boolean isAllowed() {
                return allowed;
            }

            @Override
            boolean isAuthenticated() {
                return authenticated;
            }
        };
    }

    @Test
    public void forwardsOnlySessionIdAndRequestIdNotOtherCookiesOrAuthorization() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("other", "x"), new Cookie("JSESSIONID", "SESS1"));
        request.addHeader("Authorization", "Basic c2VjcmV0");
        request.addHeader("X-Request-Id", "req-1");

        Map<String, String> headers = controller.buildHeaders(request);

        assertEquals("SESS1", headers.get("X-OpenMRS-Session-Id"));
        assertEquals("req-1", headers.get("X-Request-Id"));
        assertFalse(headers.containsKey("Authorization"));
        assertFalse(headers.containsKey("X-OpenMRS-Authorization"));
        assertFalse(headers.values().contains("x"));
    }

    @Test
    public void noSessionCookieMeansNoSessionHeader() {
        assertFalse(controller.buildHeaders(new MockHttpServletRequest()).containsKey("X-OpenMRS-Session-Id"));
    }

    @Test
    public void chatForwardsToServiceWhenAllowed() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.chat(new MockHttpServletRequest(), response, "{\"message\":\"hi\"}");

        verify(client).forward(eq("POST"), eq("/chat"), any(), eq("{\"message\":\"hi\"}"), eq(response));
    }

    @Test
    public void confirmUsesConfirmPath() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.confirm(new MockHttpServletRequest(), response, "{}");

        verify(client).forward(eq("POST"), eq("/chat/confirm"), any(), eq("{}"), eq(response));
    }

    @Test
    public void callerWithoutGatePrivilegeGets403AndNothingIsForwarded() throws Exception {
        allowed = false;
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.chat(new MockHttpServletRequest(), response, "{}");

        assertEquals(403, response.getStatus());
        assertTrue(response.getContentAsString().contains("forbidden"));
        verify(client, never()).forward(any(), any(), any(), any(), any());
    }

    @Test
    public void healthIsForwardedAsGetForAnyAuthenticatedUser() throws Exception {
        allowed = false; // the gate privilege is not needed to check reachability
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.health(new MockHttpServletRequest(), response);

        verify(client).forward(eq("GET"), eq("/health"), any(), eq(null), eq(response));
    }

    @Test
    public void healthRequiresAuthentication() throws Exception {
        authenticated = false;
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.health(new MockHttpServletRequest(), response);

        assertEquals(401, response.getStatus());
        verify(client, never()).forward(any(), any(), any(), any(), any());
    }

    @Test
    public void listForwardsOnlyPagingAsGet() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.listConversations(new MockHttpServletRequest(), response, 5, 10);
        controller.listConversations(new MockHttpServletRequest(), response, null, null);

        verify(client).forward(eq("GET"), eq("/conversations?limit=5&offset=10"), any(), eq(null), eq(response));
        verify(client).forward(eq("GET"), eq("/conversations"), any(), eq(null), eq(response));
    }

    @Test
    public void getAndDeleteForwardTheConversationId() throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.getConversation(new MockHttpServletRequest(), response, "abc-1");
        controller.deleteConversation(new MockHttpServletRequest(), response, "abc-1", null);
        controller.deleteConversation(new MockHttpServletRequest(), response, "abc-1", true);

        verify(client).forward(eq("GET"), eq("/conversations/abc-1"), any(), eq(null), eq(response));
        verify(client).forward(eq("DELETE"), eq("/conversations/abc-1"), any(), eq(null), eq(response));
        verify(client).forward(eq("DELETE"), eq("/conversations/abc-1?hard=true"), any(), eq(null), eq(response));
    }

    @Test
    public void conversationEndpointsNeedTheGatePrivilege() throws Exception {
        allowed = false;
        MockHttpServletResponse response = new MockHttpServletResponse();

        controller.listConversations(new MockHttpServletRequest(), response, null, null);

        assertEquals(403, response.getStatus());
        verify(client, never()).forward(any(), any(), any(), any(), any());
    }

    @Test
    public void conversationIdPatternRejectsPathTricks() {
        String pattern = AiServiceController.CONVERSATION_ID;
        assertTrue("abc_DEF-123".matches(pattern));
        assertFalse("..".matches(pattern));
        assertFalse("a/b".matches(pattern));
        assertFalse("a?hard=true".matches(pattern));
        assertFalse(new String(new char[65]).replace('\0', 'a').matches(pattern));
    }
    // --- the REST module's ShallowEtagHeaderFilter must not hold SSE events back ---------------------------------

    /** Runs {@code body} inside the real ShallowEtagHeaderFilter, as /ws/rest/* requests do, and reports what the
     *  client-facing response already contained while the handler was still running (i.e. before the reply ended). */
    private String receivedWhileHandlerStillRunning(MockHttpServletRequest request, MockHttpServletResponse real,
                                                    ThrowingHandler body) throws Exception {
        final String[] seen = new String[1];
        new ShallowEtagHeaderFilter().doFilter(request, real, (req, res) -> {
            try {
                body.handle(res);
            } catch (IOException | ServletException e) {
                throw e;
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
            seen[0] = real.getContentAsString(); // the reply has not finished yet at this point
            res.getOutputStream().write("event: done\n\n".getBytes());
        });
        return seen[0];
    }

    private interface ThrowingHandler {
        void handle(javax.servlet.ServletResponse response) throws Exception;
    }

    private void writeOneEventAndFlush(javax.servlet.ServletResponse response) throws Exception {
        response.getOutputStream().write("event: activity\n\n".getBytes());
        response.getOutputStream().flush();
    }

    @Test
    public void withoutTheFixTheEtagFilterWouldHoldEventsUntilTheReplyEnds() throws Exception {
        // Control for the tests below: shows the buffering they guard against is real.
        String seen = receivedWhileHandlerStillRunning(new MockHttpServletRequest("POST", "/ws/rest/v1/x"),
                new MockHttpServletResponse(), this::writeOneEventAndFlush);

        assertEquals("", seen);
    }

    @Test
    public void chatEventsReachTheClientWhileTheReplyIsStillRunning() throws Exception {
        doAnswer(invocation -> {
            writeOneEventAndFlush(invocation.getArgument(4));
            return null;
        }).when(client).forward(eq("POST"), eq("/chat"), any(), any(), any());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ws/rest/v1/bahmnicore/ai/chat");
        MockHttpServletResponse real = new MockHttpServletResponse();

        String seen = receivedWhileHandlerStillRunning(request, real, res -> {
            controller.chat(request, (javax.servlet.http.HttpServletResponse) res, "{\"message\":\"hi\"}");
        });

        assertEquals("event: activity\n\n", seen);
    }

    @Test
    public void confirmEventsReachTheClientWhileTheReplyIsStillRunning() throws Exception {
        doAnswer(invocation -> {
            writeOneEventAndFlush(invocation.getArgument(4));
            return null;
        }).when(client).forward(eq("POST"), eq("/chat/confirm"), any(), any(), any());
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/ws/rest/v1/bahmnicore/ai/chat/confirm");
        MockHttpServletResponse real = new MockHttpServletResponse();

        String seen = receivedWhileHandlerStillRunning(request, real, res -> {
            controller.confirm(request, (javax.servlet.http.HttpServletResponse) res, "{\"confirmationId\":\"c\"}");
        });

        assertEquals("event: activity\n\n", seen);
    }
}
