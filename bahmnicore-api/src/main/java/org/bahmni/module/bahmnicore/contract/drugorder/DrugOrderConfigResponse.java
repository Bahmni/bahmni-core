package org.bahmni.module.bahmnicore.contract.drugorder;


import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.List;

public class DrugOrderConfigResponse {
    private List<ConceptData> doseUnits;
    private List<ConceptData> routes;
    private List<ConceptData> durationUnits;
    private List<ConceptData> dispensingUnits;
    private String[] dosingRules;
    private List<ConceptData> dosingInstructions;
    private List<EncounterTransaction.Concept> orderAttributes;
    private List<OrderFrequencyData> frequencies = new ArrayList<>();

    public List<OrderFrequencyData> getFrequencies() {
        return frequencies;
    }

    public void setFrequencies(List<OrderFrequencyData> frequencies) {
        this.frequencies = frequencies;
    }

    public void setDoseUnits(List<ConceptData> doseUnits) {
        this.doseUnits = doseUnits;
    }

    public List<ConceptData> getDoseUnits() {
        return doseUnits;
    }

    public void setRoutes(List<ConceptData> routes) {
        this.routes = routes;
    }

    public List<ConceptData> getRoutes() {
        return routes;
    }

    public void setDurationUnits(List<ConceptData> durationUnits) {
        this.durationUnits = durationUnits;
    }

    public List<ConceptData> getDurationUnits() {
        return durationUnits;
    }

    public void setDispensingUnits(List<ConceptData> quantityUnits) {
        this.dispensingUnits = quantityUnits;
    }

    public List<ConceptData> getDispensingUnits() {
        return dispensingUnits;
    }

    public void setDosingInstructions(List<ConceptData> dosingInstructions) {
        this.dosingInstructions = dosingInstructions;
    }

    public List<ConceptData> getDosingInstructions() {
        return dosingInstructions;
    }

    public String[] getDosingRules() {
        return dosingRules;
    }

    public void setDosingRules(String[] dosingRules) {
        this.dosingRules = dosingRules;
    }


    public List<EncounterTransaction.Concept> getOrderAttributes() {
        return orderAttributes;
    }

    public void setOrderAttributes(List<EncounterTransaction.Concept> orderAttributes) {
        this.orderAttributes = orderAttributes;
    }
}
