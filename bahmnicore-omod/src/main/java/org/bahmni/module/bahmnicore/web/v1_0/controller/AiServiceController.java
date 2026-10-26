package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.client.AiServiceClient;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.filter.ShallowEtagHeaderFilter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Proxies the chat and conversation endpoints to the bahmni-ai service. OpenMRS has already authenticated the caller
 * by the time we get here. We pass the session on in X-OpenMRS-Session-Id (the same contract as the
 * template service); bahmni-ai re-validates it with OpenMRS and enforces privileges per tool.
 */
@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/ai")
public class AiServiceController extends BaseRestController {

    static final String SESSION_ID_HEADER = "X-OpenMRS-Session-Id";
    static final String REQUEST_ID_HEADER = "X-Request-Id";
    static final String GATE_PRIVILEGE = "Use AI Chatbot";
    // Same shape bahmni-ai accepts; anything else never reaches the upstream URL (no path tricks).
    static final String CONVERSATION_ID = "[A-Za-z0-9_-]{1,64}";

    private final AiServiceClient aiServiceClient;

    @Autowired
    public AiServiceController(AiServiceClient aiServiceClient) {
        this.aiServiceClient = aiServiceClient;
    }

    /** Reachability check for operators: any authenticated OpenMRS user may call it. */
    @GetMapping("/health")
    public void health(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (!isAuthenticated()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        aiServiceClient.forward("GET", "/health", buildHeaders(request), null, response);
    }

    @PostMapping("/chat")
    public void chat(HttpServletRequest request, HttpServletResponse response, @RequestBody String body)
            throws IOException {
        disableResponseBuffering(request);
        forward("/chat", request, response, body);
    }

    @PostMapping("/chat/confirm")
    public void confirm(HttpServletRequest request, HttpServletResponse response, @RequestBody String body)
            throws IOException {
        disableResponseBuffering(request);
        forward("/chat/confirm", request, response, body);
    }

    /** The caller's own conversations, newest first. Only the two paging parameters are passed on. */
    @GetMapping("/conversations")
    public void listConversations(HttpServletRequest request, HttpServletResponse response,
                                  @RequestParam(value = "limit", required = false) Integer limit,
                                  @RequestParam(value = "offset", required = false) Integer offset)
            throws IOException {
        StringBuilder path = new StringBuilder("/conversations");
        String separator = "?";
        if (limit != null) {
            path.append(separator).append("limit=").append(limit);
            separator = "&";
        }
        if (offset != null) {
            path.append(separator).append("offset=").append(offset);
        }
        forward("GET", path.toString(), request, response, null);
    }

    @GetMapping("/conversations/{id:" + CONVERSATION_ID + "}")
    public void getConversation(HttpServletRequest request, HttpServletResponse response,
                                @PathVariable("id") String id) throws IOException {
        forward("GET", "/conversations/" + id, request, response, null);
    }

    /** Soft delete; {@code hard=true} removes the conversation for good. */
    @DeleteMapping("/conversations/{id:" + CONVERSATION_ID + "}")
    public void deleteConversation(HttpServletRequest request, HttpServletResponse response,
                                   @PathVariable("id") String id,
                                   @RequestParam(value = "hard", required = false) Boolean hard)
            throws IOException {
        String path = "/conversations/" + id + (Boolean.TRUE.equals(hard) ? "?hard=true" : "");
        forward("DELETE", path, request, response, null);
    }

    /**
     * The chat endpoints stream Server-Sent Events. The REST web services module puts Spring's ShallowEtagHeaderFilter
     * on every /ws/rest/* URL; it holds the whole response in memory to compute an ETag, so without this the browser
     * would get every event at once when the reply is finished. Asking the filter not to cache keeps flush() working.
     */
    private void disableResponseBuffering(HttpServletRequest request) {
        ShallowEtagHeaderFilter.disableContentCaching(request);
    }

    private void forward(String path, HttpServletRequest request, HttpServletResponse response, String body)
            throws IOException {
        forward("POST", path, request, response, body);
    }

    private void forward(String method, String path, HttpServletRequest request, HttpServletResponse response,
                         String body) throws IOException {
        if (!isAllowed()) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"detail\":{\"code\":\"forbidden\","
                    + "\"message\":\"You are not allowed to use the AI assistant.\"}}");
            return;
        }
        aiServiceClient.forward(method, path, buildHeaders(request), body, response);
    }

    /** First layer only: the AI service checks the same privilege again, plus per-tool privileges. */
    boolean isAllowed() {
        return isAuthenticated() && Context.hasPrivilege(GATE_PRIVILEGE);
    }

    boolean isAuthenticated() {
        return Context.isAuthenticated();
    }

    Map<String, String> buildHeaders(HttpServletRequest request) {
        Map<String, String> headers = new LinkedHashMap<>();
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JSESSIONID".equals(c.getName())) {
                    headers.put(SESSION_ID_HEADER, c.getValue());
                    break;
                }
            }
        }
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId != null) {
            headers.put(REQUEST_ID_HEADER, requestId);
        }
        // Always JSON in; never forward the caller's Authorization or any other cookie.
        headers.put(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.put(HttpHeaders.ACCEPT, "text/event-stream, application/json");
        return headers;
    }
}
