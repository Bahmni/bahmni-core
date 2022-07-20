package org.bahmni.test.web.controller;

import junit.framework.Assert;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.junit.Ignore;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Ignore
@org.springframework.test.context.ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BaseWebControllerTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private RequestMappingHandlerAdapter handlerAdapter;

    @Autowired
    private List<RequestMappingHandlerMapping> handlerMappings;

    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Creates a request from the given parameters.
     * <p/>
     * The requestURI is automatically preceded with "/rest/" + RestConstants.VERSION_1.
     *
     * @param method
     * @param requestURI
     * @return
     */
    public MockHttpServletRequest request(RequestMethod method, String requestURI) {
        MockHttpServletRequest request = new MockHttpServletRequest(method.toString(), requestURI);
        request.addHeader("content-type", "application/json");
        return request;
    }

    public static class Parameter {

        public String name;

        public String value;

        public Parameter(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }

    public MockHttpServletRequest newRequest(RequestMethod method, String requestURI, Parameter... parameters) {
        MockHttpServletRequest request = request(method, requestURI);
        for (Parameter parameter : parameters) {
            request.addParameter(parameter.name, parameter.value);
        }
        return request;
    }

    public MockHttpServletRequest newRequest(RequestMethod method, String requestURI, Map<String, String> headers, Parameter... parameters) {
        MockHttpServletRequest request = newRequest(method, requestURI, parameters);
        for (String key : headers.keySet()) {
            request.addHeader(key, headers.get(key));
        }
        return request;
    }

    public MockHttpServletRequest newDeleteRequest(String requestURI, Parameter... parameters) {
        return newRequest(RequestMethod.DELETE, requestURI, parameters);
    }

    public MockHttpServletRequest newGetRequest(String requestURI, Parameter... parameters) {
        return newRequest(RequestMethod.GET, requestURI, parameters);
    }

    public MockHttpServletRequest newGetRequest(String requestURI, Map<String, String> headers, Parameter... parameters) {
        return newRequest(RequestMethod.GET, requestURI, headers, parameters);
    }

    public MockHttpServletRequest newPostRequest(String requestURI, Object content) {
        return newWriteRequest(requestURI, content, RequestMethod.POST);
    }

    public MockHttpServletRequest newPutRequest(String requestURI, Object content) {
        return newWriteRequest(requestURI, content, RequestMethod.PUT);
    }

    private MockHttpServletRequest newWriteRequest(String requestURI, Object content, RequestMethod requestMethod) {
        MockHttpServletRequest request = request(requestMethod, requestURI);
        try {
            String json = new ObjectMapper().writeValueAsString(content);
            request.setContent(json.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    public MockHttpServletRequest newPostRequest(String requestURI, String content) {
        MockHttpServletRequest request = request(RequestMethod.POST, requestURI);
        try {
            request.setContent(content.getBytes("UTF-8"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return request;
    }

    /**
     * Passes the given request to a proper controller.
     *
     * @param request
     * @return
     * @throws Exception
     */
    public MockHttpServletResponse handle(HttpServletRequest request) throws Exception {
        MockHttpServletResponse response = new MockHttpServletResponse();

        HandlerExecutionChain handlerExecutionChain = null;
        for (RequestMappingHandlerMapping handlerMapping : handlerMappings) {
            handlerExecutionChain = handlerMapping.getHandler(request);
            if (handlerExecutionChain != null) {
                break;
            }
        }
        Assert.assertNotNull("The request URI does not exist", handlerExecutionChain);

        handlerAdapter.handle(request, response, handlerExecutionChain.getHandler());

        return response;
    }

    /**
     * Deserializes the JSON response.
     *
     * @param response
     * @param type
     * @return
     * @throws Exception
     */
    public <T> T deserialize(MockHttpServletResponse response, Class<T> type) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), type);
    }

    /**
     * Deserializes the JSON response.
     *
     * @param response
     * @param typeReference
     * @return
     * @throws Exception
     */
    public <T> T deserialize(MockHttpServletResponse response, final TypeReference<T> typeReference) throws Exception {
        return objectMapper.readValue(response.getContentAsString(), typeReference);
    }
}

