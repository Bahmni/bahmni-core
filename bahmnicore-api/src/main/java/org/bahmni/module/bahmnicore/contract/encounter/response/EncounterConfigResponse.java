package org.bahmni.module.bahmnicore.contract.encounter.response;

import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;

import java.util.HashMap;
import java.util.Map;

public class EncounterConfigResponse {
    private Map<String, String> visitTypes = new HashMap<String, String>();
    private Map<String, String> encounterTypes = new HashMap<String, String>();
    private Map<String, String> orderTypes = new HashMap<String, String>();
    private Map<String, ConceptData> conceptData = new HashMap<String, ConceptData>();

    public Map<String, String> getVisitTypes() {
        return visitTypes;
    }

    public void setVisitTypes(Map<String, String> visitTypes) {
        this.visitTypes = visitTypes;
    }

    public Map<String, String> getEncounterTypes() {
        return encounterTypes;
    }

    public void setEncounterTypes(Map<String, String> encounterTypes) {
        this.encounterTypes = encounterTypes;
    }

    public void addVisitType(String name, String guid) {
        visitTypes.put(name, guid);
    }

    public void addEncounterType(String name, String guid) {
        encounterTypes.put(name, guid);
    }

    public void addConcept(String name, ConceptData conceptData) {
        this.conceptData.put(name, conceptData);
    }

    public Map<String, ConceptData> getConceptData() {
        return conceptData;
    }

    public void setConceptData(Map<String, ConceptData> conceptData) {
        this.conceptData = conceptData;
    }

    public void addOrderType(String name, String uuid) {
        orderTypes.put(name, uuid);
    }

    public Map<String, String> getOrderTypes() {
        return orderTypes;
    }

    public void setOrderTypes(Map<String, String> orderTypes) {
        this.orderTypes = orderTypes;
    }
}