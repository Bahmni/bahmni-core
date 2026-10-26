package org.bahmni.module.bahmnicore.web.v1_0.client;

import com.sun.net.httpserver.HttpServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletResponse;

import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AiServiceClientTest {

    private HttpServer server;
    private String baseUrl;
    private final AtomicReference<Map<String, String>> seenHeaders = new AtomicReference<>();

    @Before
    public void setUp() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/bahmni-ai/chat", exchange -> {
            Map<String, String> h = new HashMap<>();
            exchange.getRequestHeaders().forEach((k, v) -> h.put(k.toLowerCase(), v.get(0)));
            seenHeaders.set(h);
            exchange.getResponseHeaders().add("Content-Type", "text/event-stream");
            exchange.sendResponseHeaders(200, 0);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write("event: text\ndata: {\"delta\":\"hi\"}\n\n".getBytes(StandardCharsets.UTF_8));
                out.flush();
                out.write("event: done\ndata: {}\n\n".getBytes(StandardCharsets.UTF_8));
            }
        });
        server.createContext("/bahmni-ai/denied", exchange -> {
            byte[] body = "{\"detail\":{\"code\":\"session_expired\"}}".getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(401, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.createContext("/bahmni-ai/health", exchange -> {
            byte[] body = ("{\"status\":\"UP\",\"method\":\"" + exchange.getRequestMethod() + "\"}")
                    .getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream out = exchange.getResponseBody()) {
                out.write(body);
            }
        });
        server.createContext("/bahmni-ai/conversations", exchange -> {
            seenHeaders.set(Collections.singletonMap("request", exchange.getRequestMethod() + " "
                    + exchange.getRequestURI()));
            exchange.sendResponseHeaders(204, -1);
            exchange.close();
        });
        server.start();
        baseUrl = "http://127.0.0.1:" + server.getAddress().getPort() + "/bahmni-ai";
    }

    @After
    public void tearDown() {
        server.stop(0);
    }

    @Test
    public void streamsBodyStatusAndContentTypeAndForwardsHeaders() throws Exception {
        AiServiceClient client = new AiServiceClient(baseUrl, 2000, 5000);
        MockHttpServletResponse response = new MockHttpServletResponse();
        Map<String, String> headers = Collections.singletonMap("X-OpenMRS-Session-Id", "SESS1");

        client.forward("POST", "/chat", headers, "{\"message\":\"hi\"}", response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentType().startsWith("text/event-stream"));
        assertTrue(response.getContentAsString().contains("event: done"));
        assertEquals("SESS1", seenHeaders.get().get("x-openmrs-session-id"));
        assertEquals("no", response.getHeader("X-Accel-Buffering"));
    }

    @Test
    public void getWithoutBodyIsForwardedAsGet() throws Exception {
        AiServiceClient client = new AiServiceClient(baseUrl, 2000, 5000);
        MockHttpServletResponse response = new MockHttpServletResponse();

        client.forward("GET", "/health", Collections.<String, String>emptyMap(), null, response);

        assertEquals(200, response.getStatus());
        assertTrue(response.getContentAsString().contains("\"method\":\"GET\""));
    }

    @Test
    public void passesUpstreamErrorStatusAndBodyThrough() throws Exception {
        AiServiceClient client = new AiServiceClient(baseUrl, 2000, 5000);
        MockHttpServletResponse response = new MockHttpServletResponse();

        client.forward("POST", "/denied", Collections.<String, String>emptyMap(), "{}", response);

        assertEquals(401, response.getStatus());
        assertTrue(response.getContentAsString().contains("session_expired"));
    }

    @Test
    public void returnsBadGatewayWhenServiceIsUnreachable() throws Exception {
        AiServiceClient client = new AiServiceClient("http://127.0.0.1:1/bahmni-ai", 500, 500);
        MockHttpServletResponse response = new MockHttpServletResponse();

        client.forward("POST", "/chat", Collections.<String, String>emptyMap(), "{}", response);

        assertEquals(502, response.getStatus());
        assertTrue(response.getContentAsString().contains("ai_service_unavailable"));
    }

    @Test
    public void returnsBadGatewayWhenNotConfigured() throws Exception {
        AiServiceClient client = new AiServiceClient("", 500, 500);
        MockHttpServletResponse response = new MockHttpServletResponse();

        client.forward("POST", "/chat", Collections.<String, String>emptyMap(), "{}", response);

        assertEquals(502, response.getStatus());
    }

    @Test
    public void deleteWithNoContentResponseIsForwardedWithItsQueryString() throws Exception {
        AiServiceClient client = new AiServiceClient(baseUrl, 2000, 5000);
        MockHttpServletResponse response = new MockHttpServletResponse();

        client.forward("DELETE", "/conversations/abc?hard=true", Collections.<String, String>emptyMap(), null, response);

        assertEquals(204, response.getStatus());
        assertEquals("DELETE /bahmni-ai/conversations/abc?hard=true", seenHeaders.get().get("request"));
    }
}
