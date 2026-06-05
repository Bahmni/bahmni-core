package org.openmrs.module.bahmniemrapi.diagnosis.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniDiagnosisRequest extends BahmniDiagnosis {
    private String previousObs;
    private String encounterUuid;
    private String creatorName;

    public String getPreviousObs() {
        return previousObs;
    }

    public void setPreviousObs(String previousObs) {
        this.previousObs = previousObs;
    }

    public void setEncounterUuid(String encounterUuid) {
        this.encounterUuid = encounterUuid;
    }

    public String getEncounterUuid() {
        return encounterUuid;
    }

    public String getCreatorName() {
        return this.creatorName;
    }

    public void setCreatorName(String creatorName) {
        this.creatorName = creatorName;
    }
}
