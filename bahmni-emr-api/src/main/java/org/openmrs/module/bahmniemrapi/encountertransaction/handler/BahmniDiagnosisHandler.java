package org.openmrs.module.bahmniemrapi.encountertransaction.handler;

import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.encounter.postprocessor.EncounterTransactionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class BahmniDiagnosisHandler implements EncounterTransactionHandler {
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;
    private ObsService obsService;
    private ConceptService conceptService;


    @Autowired
    public BahmniDiagnosisHandler(BahmniDiagnosisMetadata bahmniDiagnosisMetadata, ObsService obsService, ConceptService conceptService) {
        this.bahmniDiagnosisMetadata = bahmniDiagnosisMetadata;
        this.obsService = obsService;
        this.conceptService = conceptService;
    }

    @Override
    public void forRead(Encounter encounter, EncounterTransaction encounterTransaction) {

    }

    @Override
    public void forSave(Encounter encounter, EncounterTransaction encounterTransaction) {
        List<EncounterTransaction.Diagnosis> diagnoses = encounterTransaction.getDiagnoses();
        if (!diagnoses.isEmpty()) {
            Set<Obs> obsAtTopLevel = encounter.getObsAtTopLevel(false);
            Concept bahmniDiagnosisStatusConcept = bahmniDiagnosisMetadata.diagnosisSchemaContainsStatus() ?
                    bahmniDiagnosisMetadata.getBahmniDiagnosisStatusConcept() : null;
            Concept bahmniInitialDiagnosisConcept = bahmniDiagnosisMetadata.getBahmniInitialDiagnosisConcept();
            Concept bahmniDiagnosisRevisedConcept = bahmniDiagnosisMetadata.getBahmniDiagnosisRevisedConcept();

            for (EncounterTransaction.Diagnosis diagnosis : diagnoses) {
                BahmniDiagnosisRequest bahmniDiagnosisRequest = (BahmniDiagnosisRequest) diagnosis;
                addExtraMetadata(obsAtTopLevel, bahmniDiagnosisRequest, bahmniInitialDiagnosisConcept,
                        bahmniDiagnosisStatusConcept, bahmniDiagnosisRevisedConcept);
                updateRevisedFlagOfPreviousDiagnosis(bahmniDiagnosisRequest, bahmniDiagnosisRevisedConcept);
            }
        }
    }

    private void addExtraMetadata(Set<Obs> obsAtTopLevel, BahmniDiagnosisRequest bahmniDiagnosisRequest,
                                  Concept bahmniInitialDiagnosisConcept, Concept bahmniDiagnosisStatusConcept,
                                  Concept bahmniDiagnosisRevisedConcept) {
        Obs matchingDiagnosisObs = bahmniDiagnosisMetadata.findMatchingDiagnosis(obsAtTopLevel, bahmniDiagnosisRequest);

        updateInitialDiagnosis(matchingDiagnosisObs, bahmniDiagnosisRequest, bahmniInitialDiagnosisConcept);
        if (bahmniDiagnosisStatusConcept != null) {
            updateDiagnosisStatus(matchingDiagnosisObs, bahmniDiagnosisRequest, bahmniDiagnosisStatusConcept);
        }
        updateRevisedFlag(matchingDiagnosisObs, false, bahmniDiagnosisRevisedConcept);
    }

    private void updateRevisedFlagOfPreviousDiagnosis(BahmniDiagnosisRequest bahmniDiagnosisRequest,
                                                      Concept bahmniDiagnosisRevisedConcept) {
        if(bahmniDiagnosisRequest.getPreviousObs() ==null){
            return;
        }
        Obs previousObs = obsService.getObsByUuid(bahmniDiagnosisRequest.getPreviousObs());
        updateRevisedFlag(previousObs, true, bahmniDiagnosisRevisedConcept);
        obsService.saveObs(previousObs, "Diagnosis is revised");
    }

    void updateDiagnosisStatus(Obs diagnosisObs, BahmniDiagnosis bahmniDiagnosis, Concept bahmniDiagnosisStatusConcept) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniDiagnosisStatusConcept);
        if (bahmniDiagnosis.getDiagnosisStatusConcept() != null) {
            Concept statusConcept = conceptService.getConcept(bahmniDiagnosis.getDiagnosisStatusConcept().getName());
            obs.setValueCoded(statusConcept);
            addToObsGroup(diagnosisObs, obs);
        }
        else {
            removeStatusFromObsGroup(diagnosisObs, bahmniDiagnosisStatusConcept);
        }
    }

    private void removeStatusFromObsGroup(Obs diagnosisObs, Concept bahmniDiagnosisStatusConcept) {

        Obs statusObs = diagnosisObs.getGroupMembers().stream()
                .filter(member -> member.getConcept().equals(bahmniDiagnosisStatusConcept))
                .findFirst().orElse(null);

        if (statusObs != null)
            diagnosisObs.removeGroupMember(statusObs);
    }


    private void updateInitialDiagnosis(Obs diagnosisObs, BahmniDiagnosisRequest bahmniDiagnosis,
                                        Concept bahmniInitialDiagnosisConcept) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniInitialDiagnosisConcept);
            String initialDiagnosisUuid = bahmniDiagnosis.getPreviousObs() == null && obs.getId() == null
                ? diagnosisObs.getUuid() :
                bahmniDiagnosis.getFirstDiagnosis().getExistingObs();

        obs.setValueText(initialDiagnosisUuid);
        addToObsGroup(diagnosisObs, obs);
    }


    private void updateRevisedFlag(Obs diagnosisObs, boolean value, Concept bahmniDiagnosisRevisedConcept) {
        Obs obs = findOrCreateObs(diagnosisObs, bahmniDiagnosisRevisedConcept);
        obs.setValueBoolean(value);
        addToObsGroup(diagnosisObs, obs);
    }

    private void addToObsGroup(Obs obsGroup, Obs member) {
        member.setPerson(obsGroup.getPerson());
        member.setObsDatetime(obsGroup.getObsDatetime());
        member.setLocation(obsGroup.getLocation());
        member.setEncounter(obsGroup.getEncounter());
        obsGroup.addGroupMember(member);
    }


    private Obs findOrCreateObs(Obs diagnosisObs, Concept concept) {
        for (Obs o : diagnosisObs.getGroupMembers()) {
            if (concept.equals(o.getConcept())) {
                return o;
            }
        }
        Obs obs = new Obs();
        obs.setConcept(concept);
        return obs;
    }
}
