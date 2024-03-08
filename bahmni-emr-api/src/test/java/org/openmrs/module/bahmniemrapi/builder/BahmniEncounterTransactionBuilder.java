package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class BahmniEncounterTransactionBuilder {
    private BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();

    public BahmniEncounterTransactionBuilder withObservation(BahmniObservation... bahmniObservations) {
        for (BahmniObservation bahmniObservation : bahmniObservations) {
            bahmniEncounterTransaction.addObservation(bahmniObservation);
        }
        return this;
    }

    public BahmniEncounterTransactionBuilder withVisitTypeUuid(String visitTypeUuid) {
        bahmniEncounterTransaction.setVisitTypeUuid(visitTypeUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withProviders(Set<EncounterTransaction.Provider> providerSet) {
        bahmniEncounterTransaction.setProviders(providerSet);
        return this;
    }

    public BahmniEncounterTransactionBuilder withEncounterTypeUuid(String encounterTypeUuid) {
        bahmniEncounterTransaction.setEncounterTypeUuid(encounterTypeUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withPatientUuid(String patientUuid) {
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withVisitUuid(String visitUuid) {
        bahmniEncounterTransaction.setVisitUuid(visitUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withLocationUuid(String locationUuid) {
        bahmniEncounterTransaction.setLocationUuid(locationUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withDrugOrders(List<EncounterTransaction.DrugOrder> drugOrders) {
        bahmniEncounterTransaction.setDrugOrders(drugOrders);
        return this;
    }

    public BahmniEncounterTransaction build() {
        return bahmniEncounterTransaction;
    }

    public BahmniEncounterTransactionBuilder withPatientId(String patientId) {
        bahmniEncounterTransaction.setPatientId(patientId);
        return this;
    }

    public BahmniEncounterTransactionBuilder withVisitType(String visitType) {
        bahmniEncounterTransaction.setVisitType(visitType);
        return this;
    }

    public BahmniEncounterTransactionBuilder withEncounterUuid(String encounterUuid) {
        bahmniEncounterTransaction.setEncounterUuid(encounterUuid);
        return this;
    }

    public BahmniEncounterTransactionBuilder withReason(String reason) {
        bahmniEncounterTransaction.setReason(reason);
        return this;
    }

    public BahmniEncounterTransactionBuilder withDiagnoses(BahmniDiagnosisRequest... bahmniDiagnosisRequests){
        bahmniEncounterTransaction.setBahmniDiagnoses(Arrays.asList(bahmniDiagnosisRequests));
        return this;
    }
}
