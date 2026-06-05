package org.bahmni.module.bahmnicore.contract.diseasetemplate;

import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TreeSet;

public class ObservationTemplate {
    
    private EncounterTransaction.Concept concept;
    
    private Date visitStartDate;

    private Collection<BahmniObservation> bahmniObservations;

    public EncounterTransaction.Concept getConcept() {
        return concept;
    }

    public void setConcept(EncounterTransaction.Concept concept) {
        this.concept = concept;
    }

    public Collection<BahmniObservation> getBahmniObservations() {
        return bahmniObservations == null ? new TreeSet<BahmniObservation>() : new TreeSet<>(bahmniObservations);
    }

    public void setBahmniObservations(Collection<BahmniObservation> bahmniObservations) {
        this.bahmniObservations = bahmniObservations;
    }

    public void removeBahmniObservation(BahmniObservation bahmniObservation) {
        this.bahmniObservations.remove(bahmniObservation);
    }

    public void removeBahmniObservations(Collection<BahmniObservation> bahmniObservations) {
        this.bahmniObservations.removeAll(bahmniObservations);
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getVisitStartDate() {
        return visitStartDate;
    }

    public void setVisitStartDate(Date visitStartDate) {
        this.visitStartDate = visitStartDate;
    }

    public void addBahmniObservation(BahmniObservation bahmniObservation){
        if(bahmniObservations == null){
            bahmniObservations = new ArrayList<>();
        }
        bahmniObservations.add(bahmniObservation);
    }
}
