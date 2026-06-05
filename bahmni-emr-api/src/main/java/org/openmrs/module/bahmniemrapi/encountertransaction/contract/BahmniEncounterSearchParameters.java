package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.openmrs.module.emrapi.encounter.EncounterSearchParameters;

import java.util.Date;
import java.util.List;

public class BahmniEncounterSearchParameters extends EncounterSearchParameters {
    private String patientProgramUuid;

    public BahmniEncounterSearchParameters() {
        super();
    }

    public BahmniEncounterSearchParameters(List<String> visitUuids, String patientUuid, List<String> visitTypeUuids, Date encounterDateTimeFrom, Date encounterDateTimeTo, List<String> providerUuids, List<String> encounterTypeUuids, String locationUuid, Boolean includeAll, String patientProgramUuid) {
        super(visitUuids, patientUuid, visitTypeUuids, encounterDateTimeFrom, encounterDateTimeTo, providerUuids, encounterTypeUuids, locationUuid, includeAll);
        this.patientProgramUuid = patientProgramUuid;
    }

    public String getPatientProgramUuid() {
        return patientProgramUuid;
    }

    public void setPatientProgramUuid(String patientProgramUuid) {
        this.patientProgramUuid = patientProgramUuid;
    }
}
