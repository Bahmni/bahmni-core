package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.client.TemplateServiceClient;
import org.bahmni.module.bahmnicore.service.TemplateService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public class TemplateServiceImpl implements TemplateService {

    private final TemplateServiceClient templateServiceClient;

    public TemplateServiceImpl(TemplateServiceClient templateServiceClient) {
        this.templateServiceClient = templateServiceClient;
    }

    @Override
    public ResponseEntity<String> render(HttpHeaders headers, String body) {
        return templateServiceClient.render(headers, body);
    }
}
