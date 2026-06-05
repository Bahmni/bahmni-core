package org.openmrs.module.bahmniemrapi.document.contract;

public class VisitDocumentResponse {
    private String visitUuid;
    private String encounterUuid;

    public VisitDocumentResponse(String visitUuid, String encounterUuid) {
        this.visitUuid = visitUuid;
        this.encounterUuid = encounterUuid;
    }

    public VisitDocumentResponse() {
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }
    
    public String getEncounterUuid() {
        return encounterUuid;
    }
}
