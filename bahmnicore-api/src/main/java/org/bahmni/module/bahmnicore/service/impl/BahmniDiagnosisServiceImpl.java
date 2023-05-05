package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.bahmni.module.bahmnicore.service.BahmniDiagnosisService;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ObsService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.encounter.DiagnosisMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

public class BahmniDiagnosisServiceImpl implements BahmniDiagnosisService {

    private static final String BAHMNI_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED = "bahmni.lookupExternalTerminologyServer";
    private static final boolean DEFAULT_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED = false;

    private EncounterService encounterService;
    private ObsService obsService;
    private VisitService visitService;
    private PatientService patientService;
    private DiagnosisMapper diagnosisMapper;
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;
    private ConceptService conceptService;
    private EmrApiProperties emrApiProperties;
    private AdministrationService administrationService;

    @Autowired
    public BahmniDiagnosisServiceImpl(EncounterService encounterService, ObsService obsService, VisitService visitService, PatientService patientService, DiagnosisMapper diagnosisMapper, BahmniDiagnosisMetadata bahmniDiagnosisMetadata, ConceptService conceptService, EmrApiProperties emrApiProperties, @Qualifier("adminService") AdministrationService administrationService) {
        this.encounterService = encounterService;
        this.obsService = obsService;
        this.visitService = visitService;
        this.patientService = patientService;
        this.diagnosisMapper = diagnosisMapper;
        this.bahmniDiagnosisMetadata = bahmniDiagnosisMetadata;
        this.conceptService = conceptService;
        this.emrApiProperties = emrApiProperties;
        this.administrationService = administrationService;
    }

    @Override
    public void delete(String visitDiagnosesObservationUuid) {
        Obs visitDiagnosisObs = obsService.getObsByUuid(visitDiagnosesObservationUuid);
        String initialVisitDiagnosisUuid = bahmniDiagnosisMetadata.findInitialDiagnosisUuid(visitDiagnosisObs);
        voidAllDiagnosisWithSameInitialDiagnosis(initialVisitDiagnosisUuid, visitDiagnosisObs);
    }

    private void voidAllDiagnosisWithSameInitialDiagnosis(String initialVisitDiagnosisUuid, Obs visitDiagnosisObs) {
        //find observations for this patient and concept
        List<Obs> observations = obsService.getObservationsByPersonAndConcept(visitDiagnosisObs.getPerson(), visitDiagnosisObs.getConcept());
        for (Obs observation : observations) {
            for (Obs obs : observation.getGroupMembers()) {
                if (initialVisitDiagnosisUuid.equals(obs.getValueText())) {
                    voidDiagnosis(observation);
                    break;
                }
            }
        }
    }


    private List<Diagnosis> getDiagnosisByPatient(Patient patient, Visit visit) {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();

        List<Obs> observations = obsService.getObservations(Arrays.asList((Person) patient), new ArrayList<>(visit.getEncounters()),
                Arrays.asList(bahmniDiagnosisMetadata.getDiagnosisSetConcept()), null, null, null, Arrays.asList("obsDatetime"),
                null, null, null, null, false);

        Collection<Concept> nonDiagnosisConcepts = emrApiProperties.getSuppressedDiagnosisConcepts();
        Collection<Concept> nonDiagnosisConceptSets = emrApiProperties.getNonDiagnosisConceptSets();

        for (Obs obs : observations) {
            Diagnosis diagnosis = bahmniDiagnosisMetadata.buildDiagnosisFromObsGroup(obs, nonDiagnosisConcepts, nonDiagnosisConceptSets);
            if (diagnosis != null) {
                diagnoses.add(diagnosis);
            }
        }
        return diagnoses;
    }

    private void addDiagnosisToCollectionIfRecent(List<BahmniDiagnosisRequest> bahmniDiagnosisRequests, BahmniDiagnosisRequest bahmniDiagnosisRequestNew) {
        String existingObsForNewDiag = bahmniDiagnosisRequestNew.getFirstDiagnosis().getExistingObs();
        Iterator<BahmniDiagnosisRequest> bahmniIterator = bahmniDiagnosisRequests.iterator();
        boolean addFlag = true;
        while (bahmniIterator.hasNext()) {
            BahmniDiagnosisRequest bahmniDiagnosisRequestFromList = bahmniIterator.next();
            String existingObsOfDiagFromList = bahmniDiagnosisRequestFromList.getFirstDiagnosis().getExistingObs();
            if (existingObsOfDiagFromList.equals(existingObsForNewDiag)) {
                if (bahmniDiagnosisRequestFromList.getDiagnosisDateTime().getTime() > bahmniDiagnosisRequestFromList.getDiagnosisDateTime().getTime()) {
                    bahmniIterator.remove();
                    break;
                } else {
                    addFlag = false;
                    break;
                }
            }
        }
        if (addFlag) {
            bahmniDiagnosisRequests.add(bahmniDiagnosisRequestNew);
        }
    }

    public List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndVisit(String patientUuid, String visitUuid) {
        Patient patient = patientService.getPatientByUuid(patientUuid);
        Visit visit = visitService.getVisitByUuid(visitUuid);

        if (visit == null || CollectionUtils.isEmpty(visit.getEncounters())) {
            return new ArrayList<>();
        }
        List<Diagnosis> diagnosisByVisit = getDiagnosisByPatient(patient, visit);

        List<BahmniDiagnosisRequest> bahmniDiagnosisRequests = new ArrayList<>();
        boolean diagnosisSchemaContainsStatus = bahmniDiagnosisMetadata.diagnosisSchemaContainsStatus();
        Concept bahmniDiagnosisRevised = bahmniDiagnosisMetadata.getBahmniDiagnosisRevisedConcept();
        Collection<Concept> nonDiagnosisConcepts = emrApiProperties.getSuppressedDiagnosisConcepts();
        Collection<Concept> nonDiagnosisConceptSets = emrApiProperties.getNonDiagnosisConceptSets();

        for (Diagnosis diagnosis : diagnosisByVisit) {

            EncounterTransaction.Diagnosis etDiagnosis = diagnosisMapper.convert(diagnosis);
            Obs latestObsGroup = getLatestObsGroupBasedOnAnyDiagnosis(diagnosis, bahmniDiagnosisRevised);
            Diagnosis latestDiagnosis = bahmniDiagnosisMetadata.buildDiagnosisFromObsGroup(latestObsGroup, nonDiagnosisConcepts, nonDiagnosisConceptSets); //buildDiagnosisFromObsGroup(getBahmniDiagnosisHelper().getLatestBasedOnAnyDiagnosis(diagnosis));
            EncounterTransaction.Diagnosis etLatestDiagnosis = diagnosisMapper.convert(latestDiagnosis);
            BahmniDiagnosisRequest bahmniDiagnosisRequestNew = bahmniDiagnosisMetadata.mapBahmniDiagnosis(etDiagnosis,
                    etLatestDiagnosis, true, false, diagnosisSchemaContainsStatus, true);
            addDiagnosisToCollectionIfRecent(bahmniDiagnosisRequests, bahmniDiagnosisRequestNew);
        }

        return bahmniDiagnosisRequests;
    }

    private Obs getLatestObsGroupBasedOnAnyDiagnosis(Diagnosis diagnosis, Concept bahmniDiagnosisRevised) {
        String initialDiagnosisUuid = bahmniDiagnosisMetadata.findInitialDiagnosisUuid(diagnosis.getExistingObs());

        List<Obs> observations = obsService.getObservations(Arrays.asList(diagnosis.getExistingObs().getPerson()), null,
                Arrays.asList(bahmniDiagnosisRevised),
                Arrays.asList(conceptService.getFalseConcept()), null, null, null,
                null, null, null, null, false);

        for (Obs obs : observations) {
            Obs diagnosisObsGroup = obs.getObsGroup();
            //This is main diagosis group. Now, find the initialDiagnosis.  Also ensure that this is visitDiagnosis??
            Obs bahmniInitialDiagnosis = bahmniDiagnosisMetadata.findInitialDiagnosis(diagnosisObsGroup);
            if (initialDiagnosisUuid.equals(bahmniInitialDiagnosis.getValueText())) {
                return diagnosisObsGroup;
            }
        }

        return null;
    }

    public List<BahmniDiagnosisRequest> getBahmniDiagnosisByPatientAndDate(String patientUuid, String date) throws ParseException {
        Patient patient = patientService.getPatientByUuid(patientUuid);

        Date fromDate = date != null ? new SimpleDateFormat("yyyy-MM-dd").parse(date) : null;
        List<Diagnosis> diagnosisByPatientAndDate = Context.getService(DiagnosisService.class).getDiagnoses(patient, fromDate);

        List<BahmniDiagnosisRequest> bahmniDiagnosisRequests = new ArrayList<>();
        boolean diagnosisSchemaContainsStatus = bahmniDiagnosisMetadata.diagnosisSchemaContainsStatus();

        for (Diagnosis diagnosis : diagnosisByPatientAndDate) {
            EncounterTransaction.Diagnosis etDiagnosis = diagnosisMapper.convert(diagnosis);
            BahmniDiagnosisRequest bahmniDiagnosisRequest = bahmniDiagnosisMetadata.mapBahmniDiagnosis(etDiagnosis,
                    null, true, false, diagnosisSchemaContainsStatus,false);

            if (bahmniDiagnosisRequest != null) {
                bahmniDiagnosisRequests.add(bahmniDiagnosisRequest);
            }
        }

        return bahmniDiagnosisRequests;
    }

    private void voidDiagnosis(Obs observation) {
        voidObsAndItsChildren(observation);
        encounterService.saveEncounter(observation.getEncounter());
    }

    private void voidObsAndItsChildren(Obs obs) {
        obs.setVoided(true);
        if (obs.getGroupMembers() == null)
            return;
        for (Obs childObs : obs.getGroupMembers()) {
            voidObsAndItsChildren(childObs);
        }
    }

    @Override
    public boolean isExternalTerminologyServerLookupNeeded() {
        String externalTSLookupNeeded = administrationService.getGlobalProperty(BAHMNI_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED);
        return StringUtils.isNotBlank(externalTSLookupNeeded) ? Boolean.valueOf(externalTSLookupNeeded) : DEFAULT_EXTERNAL_TERMINOLOGY_SERVER_LOOKUP_NEEDED;
    }

    @Override
    public Collection<Concept> getDiagnosisSets() {
        return emrApiProperties.getDiagnosisSets();
    }

    @Override
    public List<ConceptSource> getConceptSourcesForDiagnosisSearch() {
        return emrApiProperties.getConceptSourcesForDiagnosisSearch();
    }
}
