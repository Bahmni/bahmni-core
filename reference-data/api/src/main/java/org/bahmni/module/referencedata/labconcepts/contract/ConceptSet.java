package org.bahmni.module.referencedata.labconcepts.contract;

import java.util.ArrayList;
import java.util.List;

public class ConceptSet extends ConceptCommon {
    private List<String> children;

    public ConceptSet() {
        super();
    }

    public ConceptSet(String uuid, String name, String conceptDescription, String conceptClass, String conceptShortname, List<ConceptReferenceTerm> referenceTerms, List<String> children) {
        super(uuid, name, conceptDescription, conceptClass, conceptShortname, referenceTerms, "N/A");
        this.children = children;
    }

    public List<String> getChildren() {
        return children == null ? new ArrayList<String>() : children;
    }

    public void setChildren(List<String> children) {
        this.children = children;
    }
}
