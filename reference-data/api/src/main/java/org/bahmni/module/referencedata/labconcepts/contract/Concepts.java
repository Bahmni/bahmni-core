package org.bahmni.module.referencedata.labconcepts.contract;

import java.util.List;

public class Concepts {
    private List<Concept> conceptList;
    private List<ConceptSet> conceptSetList;

    public List<Concept> getConceptList() {
        return conceptList;
    }

    public void setConceptList(List<Concept> conceptList) {
        this.conceptList = conceptList;
    }

    public List<ConceptSet> getConceptSetList() {
        return conceptSetList;
    }

    public void setConceptSetList(List<ConceptSet> conceptSetList) {
        this.conceptSetList = conceptSetList;
    }
}
