package org.bahmni.module.bahmnicore.service;

import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;

import java.text.ParseException;
import java.util.Collection;
import java.util.List;

public interface BahmniDiagnosisService {
    void delete(String diagnosisObservationUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndVisit(String patientUuid,String visitUuid);
    List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndDate(String patientUuid, String date) throws ParseException;

    boolean isExternalTerminologyServerLookupNeeded();

    Collection<Concept> getDiagnosisSets();

    List<ConceptSource> getConceptSourcesForDiagnosisSearch();
}
