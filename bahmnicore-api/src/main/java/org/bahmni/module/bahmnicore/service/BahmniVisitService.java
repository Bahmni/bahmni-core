package org.bahmni.module.bahmnicore.service;

import org.openmrs.Encounter;
import org.openmrs.Visit;

import java.util.List;

public interface BahmniVisitService {
    public Visit getLatestVisit(String patientUuid, String conceptName);

    Visit getVisitSummary(String visitUuid);

    List<Encounter> getAdmitAndDischargeEncounters(Integer visitId);
}
