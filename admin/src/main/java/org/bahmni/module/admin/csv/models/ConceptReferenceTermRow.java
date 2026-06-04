package org.bahmni.module.admin.csv.models;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.annotation.CSVHeader;

public class ConceptReferenceTermRow {
    @CSVHeader(name = "reference-term-source")
    private String referenceTermSource;

    @CSVHeader(name = "reference-term-code")
    private String referenceTermCode;

    @CSVHeader(name = "reference-term-relationship")
    private String referenceTermRelationship;

    public String getReferenceTermSource() {
        return referenceTermSource;
    }

    public void setReferenceTermSource(String referenceTermSource) {
        this.referenceTermSource = referenceTermSource;
    }

    public String getReferenceTermCode() {
        return referenceTermCode;
    }

    public void setReferenceTermCode(String referenceTermCode) {
        this.referenceTermCode = referenceTermCode;
    }

    public String getReferenceTermRelationship() {
        return referenceTermRelationship;
    }

    public void setReferenceTermRelationship(String referenceTermRelationship) {
        this.referenceTermRelationship = referenceTermRelationship;
    }

    public ConceptReferenceTermRow() {

    }

    public ConceptReferenceTermRow(String referenceTermSource, String referenceTermCode, String referenceTermRelationship) {
        this.referenceTermSource = referenceTermSource;
        this.referenceTermCode = referenceTermCode;
        this.referenceTermRelationship = referenceTermRelationship;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(referenceTermSource) && StringUtils.isBlank(referenceTermCode) && StringUtils.isBlank(referenceTermRelationship);
    }

    public String[] getRowValues() {
        return new String[]{referenceTermSource, referenceTermCode, referenceTermRelationship};
    }

    public ConceptReferenceTermRow getHeaders() {
        return new ConceptReferenceTermRow("reference-term-source", "reference-term-code", "reference-term-relationship");
    }
}
