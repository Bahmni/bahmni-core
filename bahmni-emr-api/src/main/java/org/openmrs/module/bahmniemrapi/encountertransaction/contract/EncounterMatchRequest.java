package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import java.util.Date;
import java.util.List;

public class EncounterMatchRequest {

    private String patientUuid;
    private String visitUuid;
    private List<String> encounterTypeUuids;
    private List<String> providerUuids;
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

    public List<String> getEncounterTypeUuids() {
        return encounterTypeUuids;
    }

    public void setEncounterTypeUuids(List<String> encounterTypeUuids) {
        this.encounterTypeUuids = encounterTypeUuids;
    }

    public List<String> getProviderUuids() {
        return providerUuids;
    }

    public void setProviderUuids(List<String> providerUuids) {
        this.providerUuids = providerUuids;
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
