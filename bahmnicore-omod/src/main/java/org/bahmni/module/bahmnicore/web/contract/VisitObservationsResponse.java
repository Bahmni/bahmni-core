package org.bahmni.module.bahmnicore.web.contract;

import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;

import java.util.Collection;

public class VisitObservationsResponse {

    private String visitUuid;
    private Collection<BahmniObservation> observations;

    public VisitObservationsResponse() {
    }

    public VisitObservationsResponse(String visitUuid, Collection<BahmniObservation> observations) {
        this.visitUuid = visitUuid;
        this.observations = observations;
    }

    public String getVisitUuid() {
        return visitUuid;
    }

    public void setVisitUuid(String visitUuid) {
        this.visitUuid = visitUuid;
    }

    public Collection<BahmniObservation> getObservations() {
        return observations;
    }

    public void setObservations(Collection<BahmniObservation> observations) {
        this.observations = observations;
    }
}
