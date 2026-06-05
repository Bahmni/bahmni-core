package org.bahmni.module.bahmnicore.contract.encounter.data;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.emrapi.utils.CustomJsonDateSerializer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EncounterModifierObservation {
    private BahmniObservation bahmniObservation = new BahmniObservation();
    private Collection<EncounterModifierObservation> groupMembers = new ArrayList<>();

    public EncounterModifierObservation(BahmniObservation bahmniObservation) {
        this.bahmniObservation = bahmniObservation;
    }

    public EncounterModifierObservation() {
    }

    public void addGroupMember(BahmniObservation observation) {
        bahmniObservation.addGroupMember(observation);
    }

    public void addProvider(EncounterTransaction.Provider provider) {
        bahmniObservation.addProvider(provider);
    }

    public String getComment() {
        return bahmniObservation.getComment();
    }

    public EncounterTransaction.Concept getConcept() {
        return bahmniObservation.getConcept();
    }

    public String getConceptNameToDisplay() {
        return bahmniObservation.getConceptNameToDisplay();
    }

    public Integer getConceptSortWeight() {
        return bahmniObservation.getConceptSortWeight();
    }

    public String getConceptUuid() {
        return bahmniObservation.getConceptUuid();
    }

    public Long getDuration() {
        return bahmniObservation.getDuration();
    }

    public Date getEncounterDateTime() {
        return bahmniObservation.getEncounterDateTime();
    }

    public String getEncounterUuid() {
        return bahmniObservation.getEncounterUuid();
    }

    public Boolean getIsAbnormal() {
        return bahmniObservation.getIsAbnormal();
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getObservationDateTime() {
        return bahmniObservation.getObservationDateTime();
    }

    public String getObsGroupUuid() {
        return bahmniObservation.getObsGroupUuid();
    }

    public String getOrderUuid() {
        return bahmniObservation.getOrderUuid();
    }

    public Set<EncounterTransaction.Provider> getProviders() {
        return bahmniObservation.getProviders();
    }

    public ObsRelationship getTargetObsRelation() {
        return bahmniObservation.getTargetObsRelation();
    }

    public String getType() {
        return bahmniObservation.getType();
    }

    public String getUuid() {
        return bahmniObservation.getUuid();
    }

    public Object getValue() {
        return bahmniObservation.getValue();
    }

    public String getValueAsString() {
        return bahmniObservation.getValueAsString();
    }

    @JsonSerialize(using = CustomJsonDateSerializer.class)
    public Date getVisitStartDateTime() {
        return bahmniObservation.getVisitStartDateTime();
    }

    public boolean getVoided() {
        return bahmniObservation.getVoided();
    }

    public String getVoidReason() {
        return bahmniObservation.getVoidReason();
    }

    public boolean hasTargetObsRelation() {
        return bahmniObservation.hasTargetObsRelation();
    }

    public Boolean isAbnormal() {
        return bahmniObservation.isAbnormal();
    }

    public boolean isSameAs(EncounterTransaction.Observation encounterTransactionObservation) {
        return bahmniObservation.isSameAs(encounterTransactionObservation);
    }

    public boolean isSameAs(Obs obs) {
        return bahmniObservation.isSameAs(obs);
    }

    public void removeGroupMembers(Collection<BahmniObservation> observations) {
        bahmniObservation.removeGroupMembers(observations);
    }

    public void setAbnormal(Boolean abnormal) {
        bahmniObservation.setAbnormal(abnormal);
    }

    public BahmniObservation setComment(String comment) {
        return bahmniObservation.setComment(comment);
    }

    public BahmniObservation setConcept(EncounterTransaction.Concept concept) {
        return bahmniObservation.setConcept(concept);
    }

    public void setConceptSortWeight(Integer conceptSortWeight) {
        bahmniObservation.setConceptSortWeight(conceptSortWeight);
    }

    public void setDuration(Long duration) {
        bahmniObservation.setDuration(duration);
    }

    public void setEncounterDateTime(Date encounterDateTime) {
        bahmniObservation.setEncounterDateTime(encounterDateTime);
    }

    public void setEncounterTransactionObservation(EncounterTransaction.Observation encounterTransactionObservation) {
        bahmniObservation.setEncounterTransactionObservation(encounterTransactionObservation);
    }

    public void setEncounterUuid(String encounterUuid) {
        bahmniObservation.setEncounterUuid(encounterUuid);
    }

    public BahmniObservation setObservationDateTime(Date observationDateTime) {
        return bahmniObservation.setObservationDateTime(observationDateTime);
    }

    public void setObsGroupUuid(String obsGroupUuid) {
        bahmniObservation.setObsGroupUuid(obsGroupUuid);
    }

    public BahmniObservation setOrderUuid(String orderUuid) {
        return bahmniObservation.setOrderUuid(orderUuid);
    }

    public void setProviders(Set<EncounterTransaction.Provider> providers) {
        bahmniObservation.setProviders(providers);
    }

    public void setTargetObsRelation(ObsRelationship targetObsRelation) {
        bahmniObservation.setTargetObsRelation(targetObsRelation);
    }

    public void setType(String type) {
        bahmniObservation.setType(type);
    }

    public BahmniObservation setUuid(String uuid) {
        return bahmniObservation.setUuid(uuid);
    }

    public BahmniObservation setValue(Object value) {
        return bahmniObservation.setValue(value);
    }

    public void setVisitStartDateTime(Date visitStartDateTime) {
        bahmniObservation.setVisitStartDateTime(visitStartDateTime);
    }

    public BahmniObservation setVoided(boolean voided) {
        return bahmniObservation.setVoided(voided);
    }

    public BahmniObservation setVoidReason(String voidReason) {
        return bahmniObservation.setVoidReason(voidReason);
    }

    public EncounterTransaction.Observation toETObservation() {
        return bahmniObservation.toETObservation();
    }

    public static List<EncounterTransaction.Observation> toETObsFromBahmniObs(Collection<BahmniObservation> bahmniObservations) {
        return BahmniObservation.toETObsFromBahmniObs(bahmniObservations);
    }

    public Collection<EncounterModifierObservation> getGroupMembers() {
        return groupMembers;
    }

    public void setGroupMembers(Collection<EncounterModifierObservation> groupMembers) {
        this.groupMembers = groupMembers;
    }

    public Boolean isUnknown() {
        return bahmniObservation.isUnknown();
    }

    public void setUnknown(Boolean unknown) {
        bahmniObservation.setUnknown(unknown);
    }

}
