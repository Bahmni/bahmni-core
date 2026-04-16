package org.bahmni.module.bahmnicore.web.contract;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class BahmniObservationsBatchRequest {

    private List<String> visitUuids;
    private List<String> concept;
    private String scope;
    private List<String> obsIgnoreList;
    private Boolean filterObsWithOrders;

    public BahmniObservationsBatchRequest() {
    }

    public List<String> getVisitUuids() {
        return visitUuids;
    }

    public void setVisitUuids(List<String> visitUuids) {
        this.visitUuids = visitUuids;
    }

    public List<String> getConcept() {
        return concept;
    }

    public void setConcept(List<String> concept) {
        this.concept = concept;
    }

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public List<String> getObsIgnoreList() {
        return obsIgnoreList;
    }

    public void setObsIgnoreList(List<String> obsIgnoreList) {
        this.obsIgnoreList = obsIgnoreList;
    }

    public Boolean getFilterObsWithOrders() {
        return filterObsWithOrders == null ? true : filterObsWithOrders;
    }

    public void setFilterObsWithOrders(Boolean filterObsWithOrders) {
        this.filterObsWithOrders = filterObsWithOrders;
    }
}
