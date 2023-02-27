package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.api.EncounterService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.accessionnote.mapper.AccessionNotesMapper;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class BahmniEncounterTransactionMapper {
    private AccessionNotesMapper accessionNotesMapper;
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;
    private ObsRelationshipMapper obsRelationshipMapper;
    private PatientService patientService;
    private EncounterService encounterService;
    private ETObsToBahmniObsMapper fromETObsToBahmniObs;

    @Autowired
    public BahmniEncounterTransactionMapper(AccessionNotesMapper accessionNotesMapper,
                                            BahmniDiagnosisMetadata bahmniDiagnosisMetadata,
                                            ObsRelationshipMapper obsRelationshipMapper,
                                            PatientService patientService,
                                            EncounterService encounterService,
                                            ETObsToBahmniObsMapper fromETObsToBahmniObs) {
        this.accessionNotesMapper = accessionNotesMapper;
        this.bahmniDiagnosisMetadata = bahmniDiagnosisMetadata;
        this.obsRelationshipMapper = obsRelationshipMapper;
        this.patientService = patientService;
        this.encounterService = encounterService;
        this.fromETObsToBahmniObs = fromETObsToBahmniObs;
    }

    public BahmniEncounterTransaction map(EncounterTransaction encounterTransaction, boolean includeAll) {
        System.out.println("4. Map function reached : " + new Timestamp(new java.util.Date().getTime()));
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction(encounterTransaction);
        List<BahmniDiagnosisRequest> bahmniDiagnoses = bahmniDiagnosisMetadata.map(encounterTransaction.getDiagnoses(), includeAll);
        System.out.println("4. BahmniDiagnosisMetadata map completed : " + new Timestamp(new java.util.Date().getTime()));
        bahmniEncounterTransaction.setBahmniDiagnoses(bahmniDiagnoses);
        System.out.println("4. setBahmniDiagnosis completed : " + new Timestamp(new java.util.Date().getTime()));
        bahmniEncounterTransaction.setAccessionNotes(accessionNotesMapper.map(encounterTransaction));
        System.out.println("4. setAccessionNotes completed : " + new Timestamp(new java.util.Date().getTime()));
        AdditionalBahmniObservationFields additionalBahmniObservationFields = new AdditionalBahmniObservationFields(encounterTransaction.getEncounterUuid(), encounterTransaction.getEncounterDateTime(), null,null);
        System.out.println("4. Additional Observation fields completed : " + new Timestamp(new java.util.Date().getTime()));
        additionalBahmniObservationFields.setProviders(encounterTransaction.getProviders());
        System.out.println("4. setProviders completed : " + new Timestamp(new java.util.Date().getTime()));
        List<BahmniObservation> bahmniObservations = fromETObsToBahmniObs.create(encounterTransaction.getObservations(), additionalBahmniObservationFields);
        System.out.println("4. fromETObsToBahmniObs.create completed : " + new Timestamp(new java.util.Date().getTime()));
        bahmniEncounterTransaction.setObservations(obsRelationshipMapper.map(bahmniObservations, encounterTransaction.getEncounterUuid()));
        System.out.println("4. setObservations completed : " + new Timestamp(new java.util.Date().getTime()));
        addPatientIdentifier(bahmniEncounterTransaction, encounterTransaction);
        addEncounterType(encounterTransaction, bahmniEncounterTransaction);
        System.out.println("4. Map function completed : " + new Timestamp(new java.util.Date().getTime()));
        return bahmniEncounterTransaction;
    }

    private void addEncounterType(EncounterTransaction encounterTransaction, BahmniEncounterTransaction bahmniEncounterTransaction) {
        EncounterType encounterType = encounterService.getEncounterTypeByUuid(encounterTransaction.getEncounterTypeUuid());
        if (encounterType != null) {
            bahmniEncounterTransaction.setEncounterType(encounterType.getName());
        }
    }

    private void addPatientIdentifier(BahmniEncounterTransaction bahmniEncounterTransaction, EncounterTransaction encounterTransaction) {
        Patient patient = patientService.getPatientByUuid(encounterTransaction.getPatientUuid());
        if (patient != null) {
            PatientIdentifier patientIdentifier = patient.getPatientIdentifier();
            if(patientIdentifier != null){
                bahmniEncounterTransaction.setPatientId(patientIdentifier.getIdentifier());
            }
        }
    }
}
