/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;
import org.xml.sax.InputSource;

import javax.servlet.http.HttpServletRequest;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;

/**
 * Facilitates testing controllers.
 */
public abstract class BahmniMainResourceControllerTest extends BaseIntegrationTest {

	@Autowired
	private RequestMappingHandlerAdapter handlerAdapter;

	@Autowired
	private List<RequestMappingHandlerMapping> handlerMappings;

	/**
	 * Creates a request from the given parameters.
	 * <p>
	 * The requestURI is automatically preceded with "/rest/" + RestConstants.VERSION_1.
	 *
	 * @param method
	 * @param requestURI
	 * @return
	 */
	public MockHttpServletRequest request(RequestMethod method, String requestURI) {
		MockHttpServletRequest request = new MockHttpServletRequest(method.toString(), "/rest/" + getNamespace() + "/"
		        + requestURI);
		request.addHeader("content-type", "application/json");
		return request;
	}

	/**
	 * Override this method to test a different namespace than v1.
	 *
	 * @return the namespace
	 */
	public String getNamespace() {
		return RestConstants.VERSION_1;
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

	public MockHttpServletRequest newDeleteRequest(String requestURI, Parameter... parameters) {
		return newRequest(RequestMethod.DELETE, requestURI, parameters);
	}

	public MockHttpServletRequest newGetRequest(String requestURI, Parameter... parameters) {
		return newRequest(RequestMethod.GET, requestURI, parameters);
	}

	public MockHttpServletRequest newPostRequest(String requestURI, Object content) {
		MockHttpServletRequest request = request(RequestMethod.POST, requestURI);
		try {
			String json = new ObjectMapper().writeValueAsString(content);
			request.setContent(json.getBytes("UTF-8"));
		}
		catch (Exception e) {
			throw new RuntimeException(e);
		}
		return request;
	}

	public MockHttpServletRequest newPostRequest(String requestURI, String content) {
		MockHttpServletRequest request = request(RequestMethod.POST, requestURI);
		try {
			request.setContent(content.getBytes("UTF-8"));
		}
		catch (Exception e) {
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
	 * @return
	 * @throws Exception
	 */
	public SimpleObject deserialize(MockHttpServletResponse response) throws Exception {
		return new ObjectMapper().readValue(response.getContentAsString(), SimpleObject.class);
	}


	/**
	 * @return the URI of the resource
	 */
	public abstract String getURI();

	/**
	 * @return the uuid of an existing object
	 */
	public abstract String getUuid();

	/**
	 * @return the count of all not retired/voided objects
	 */
	public abstract long getAllCount();

	/**
	 * Evaluates an XPath expression on a XML string
	 *
	 * @param xml
	 * @param xPath
	 * @return
	 * @throws XPathExpressionException
	 */
	protected String evaluateXPath(String xml, String xPath) throws XPathExpressionException {
		InputSource source = new InputSource(new StringReader(xml));
		XPath xpath = XPathFactory.newInstance().newXPath();
		return xpath.evaluate(xPath, source);
	}

	/**
	 * Prints an XML string indented
	 *
	 * @param xml
	 * @throws TransformerException
	 */
	protected void printXML(String xml) throws TransformerException {

		Source xmlInput = new StreamSource(new StringReader(xml));
		StringWriter stringWriter = new StringWriter();

		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.setOutputProperty(OutputKeys.INDENT, "yes");
		transformer.transform(xmlInput, new StreamResult(stringWriter));

		System.out.println(stringWriter.toString());
	}

}
