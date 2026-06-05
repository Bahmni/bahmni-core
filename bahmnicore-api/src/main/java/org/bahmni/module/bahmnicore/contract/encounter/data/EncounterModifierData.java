package org.bahmni.module.bahmnicore.contract.encounter.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.Date;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterModifierData {
    private List<EncounterModifierObservation> encounterModifierObservations;
    private List<EncounterTransaction.DrugOrder> drugOrders;
    private ConceptData conceptSetData;
    private String patientUuid;
    private Date encounterDateTime;

    public EncounterModifierData(ConceptData conceptSetData, List<EncounterTransaction.DrugOrder> drugOrders, Date encounterDateTime, List<EncounterModifierObservation> encounterModifierObservations, String patientUuid) {
        this.conceptSetData = conceptSetData;
        this.drugOrders = drugOrders;
        this.encounterDateTime = encounterDateTime;
        this.encounterModifierObservations = encounterModifierObservations;
        this.patientUuid = patientUuid;
    }

    public EncounterModifierData() {
    }

    public ConceptData getConceptSetData() {
        return conceptSetData;
    }

    public void setConceptSetData(ConceptData conceptSetData) {
        this.conceptSetData = conceptSetData;
    }

    public List<EncounterTransaction.DrugOrder> getDrugOrders() {
        return drugOrders;
    }

    public void setDrugOrders(List<EncounterTransaction.DrugOrder> drugOrders) {
        this.drugOrders = drugOrders;
    }

    public List<EncounterModifierObservation> getEncounterModifierObservations() {
        return encounterModifierObservations;
    }

    public void setEncounterModifierObservations(List<EncounterModifierObservation> encounterModifierObservations) {
        this.encounterModifierObservations = encounterModifierObservations;
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getEncounterDateTime() {
        return encounterDateTime;
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        this.encounterDateTime = encounterDateTime;
    }

    public String getPatientUuid() {
        return patientUuid;
    }

    public void setPatientUuid(String patientUuid) {
        this.patientUuid = patientUuid;
    }
}