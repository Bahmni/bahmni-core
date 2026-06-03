/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.bahmni.module.bahmnicore.web.v1_0.client;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Component
public class TemplateServiceClient {

    private static final Log log = LogFactory.getLog(TemplateServiceClient.class);

    private static final String URL_KEY = "template.service.url";
    private static final String CONNECT_TIMEOUT_KEY = "template.service.connectTimeoutInMilliseconds";
    private static final String READ_TIMEOUT_KEY = "template.service.readTimeoutInMilliseconds";
    private static final int DEFAULT_CONNECT_TIMEOUT = 5000;
    private static final int DEFAULT_READ_TIMEOUT = 10000;
    private static final String RENDER_PATH = "/api/render";

    private final RestTemplate restTemplate;
    private final String baseUrl;

    @Autowired
    public TemplateServiceClient() {
        this.baseUrl = BahmniCoreProperties.getProperty(URL_KEY);
        this.restTemplate = buildRestTemplate();
    }

    TemplateServiceClient(RestTemplate restTemplate, String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ResponseEntity<String> render(HttpHeaders headers, String body) {
        try {
            return restTemplate.exchange(baseUrl + RENDER_PATH, HttpMethod.POST,
                    new HttpEntity<>(body, headers), String.class);
        } catch (HttpStatusCodeException e) {
            return ResponseEntity.status(e.getStatusCode()).body(e.getResponseBodyAsString());
        } catch (RestClientException e) {
            log.error("Template service unreachable at " + baseUrl, e);
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body("Template service is unavailable: " + e.getMessage());
        }
    }

    private RestTemplate buildRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(parseTimeout(CONNECT_TIMEOUT_KEY, DEFAULT_CONNECT_TIMEOUT));
        factory.setReadTimeout(parseTimeout(READ_TIMEOUT_KEY, DEFAULT_READ_TIMEOUT));
        return new RestTemplate(factory);
    }

    private int parseTimeout(String key, int defaultValue) {
        int value = NumberUtils.toInt(StringUtils.trim(BahmniCoreProperties.getProperty(key)), defaultValue);
        return value > 0 ? value : defaultValue;
    }
}
