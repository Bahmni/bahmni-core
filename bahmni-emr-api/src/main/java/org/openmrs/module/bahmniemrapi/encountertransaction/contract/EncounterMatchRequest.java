package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import java.util.Date;

public class EncounterMatchRequest {

    private String patientUuid;
    private String visitUuid;
    private String providerUuid;
    private String locationUuid;
    private Date encounterDateTime;
    private String patientProgramUuid;
    private Boolean includeAll;

    public EncounterMatchRequest() {
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public String getProviderUuid() {
        return providerUuid;
    }

    public void setProviderUuid(String providerUuid) {
        this.providerUuid = providerUuid;
    }

    public String getLocationUuid() {
        return locationUuid;
    }

    public void setLocationUuid(String locationUuid) {
        this.locationUuid = locationUuid;
    }

    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public String getPatientProgramUuid() {
        return patientProgramUuid;
    }

    public void setPatientProgramUuid(String patientProgramUuid) {
        this.patientProgramUuid = patientProgramUuid;
    }

    public Boolean getIncludeAll() {
        return includeAll;
    }

    public void setIncludeAll(Boolean includeAll) {
        this.includeAll = includeAll;
    }
}
