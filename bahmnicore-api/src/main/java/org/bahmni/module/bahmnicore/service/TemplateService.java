package org.bahmni.module.bahmnicore.service;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;

public interface TemplateService {
    ResponseEntity<String> render(HttpHeaders headers, String body);
}
