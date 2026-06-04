package org.openmrs.module.bahmniemrapi.document.service;

import org.openmrs.Encounter;
import org.openmrs.module.bahmniemrapi.document.contract.VisitDocumentRequest;

public interface VisitDocumentService {
    Encounter upload(VisitDocumentRequest visitDocumentRequest);
}
