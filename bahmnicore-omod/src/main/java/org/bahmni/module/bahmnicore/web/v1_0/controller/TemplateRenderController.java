package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.web.v1_0.client.TemplateServiceClient;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/template/api")
public class TemplateRenderController extends BaseRestController {

    private Log log = LogFactory.getLog(this.getClass());

    static final String SESSION_ID_HEADER = "X-OpenMRS-Session-Id";
    static final String AUTH_HEADER = "X-OpenMRS-Authorization";

    private final TemplateServiceClient templateServiceClient;

    @Autowired
    public TemplateRenderController(TemplateServiceClient templateServiceClient) {
        this.templateServiceClient = templateServiceClient;
    }

    @ResponseBody
    @GetMapping("/templates")
    public ResponseEntity<String> getTemplates(HttpServletRequest request) {
        ResponseEntity<String> serviceResponse = templateServiceClient.getTemplates(buildSessionHeaders(request));
        return buildResponse(serviceResponse);
    }

    @ResponseBody
    @PostMapping("/render")
    public ResponseEntity<String> render(HttpServletRequest request, @RequestBody String body) {
        ResponseEntity<String> serviceResponse = templateServiceClient.render(buildSessionHeaders(request), body);
        return buildResponse(serviceResponse);
    }

    private ResponseEntity<String> buildResponse(ResponseEntity<String> serviceResponse) {
        HttpHeaders responseHeaders = new HttpHeaders();
        MediaType contentType = serviceResponse.getHeaders().getContentType();
        if (contentType != null) {
            responseHeaders.setContentType(contentType);
        }
        return ResponseEntity.status(serviceResponse.getStatusCode())
                .headers(responseHeaders)
                .body(serviceResponse.getBody());
    }

    HttpHeaders buildSessionHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();

        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("JSESSIONID".equals(c.getName())) {
                    headers.set(SESSION_ID_HEADER, c.getValue());
                    break;
                }
            }
        }

        String auth = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (auth != null) {
            headers.set(AUTH_HEADER, auth);
        }

        String contentType = request.getContentType();
        if (contentType != null) {
            headers.setContentType(MediaType.parseMediaType(contentType));
        }

        return headers;
    }
}
