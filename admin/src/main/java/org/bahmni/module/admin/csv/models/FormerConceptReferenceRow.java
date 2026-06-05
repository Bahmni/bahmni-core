package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.annotation.CSVHeader;
import org.bahmni.csv.annotation.CSVRepeatingHeaders;

import java.util.ArrayList;
import java.util.List;

public class FormerConceptReferenceRow extends CSVEntity {
    @CSVHeader(name = "concept-name")
    private String conceptName;

    @CSVRepeatingHeaders(names = {"reference-term-source", "reference-term-code", "reference-term-relationship"}, type = ConceptReferenceTermRow.class)
    private List<ConceptReferenceTermRow> referenceTerms = new ArrayList<>();


    public String getConceptName() {
        return conceptName;
    }

    public void setConceptName(String conceptName) {
        this.conceptName = conceptName;
    }

    public List<ConceptReferenceTermRow> getReferenceTerms() {
        return referenceTerms;
    }

    public void setReferenceTerms(List<ConceptReferenceTermRow> referenceTerms) {
        this.referenceTerms = referenceTerms;
    }
}
