package org.bahmni.module.referencedata.labconcepts.contract;

public class ConceptReferenceTerm {

    private String referenceTermName;
    private String referenceTermCode;
    private String referenceTermRelationship;
    private String referenceTermSource;

    private String referenceDescription;
    private String referenceVersion;

    public ConceptReferenceTerm() {
    }

    public ConceptReferenceTerm(String conceptReferenceTermCode, String conceptReferenceTermRelationship, String conceptReferenceTermSource) {
        this.referenceTermCode = conceptReferenceTermCode;
        this.referenceTermRelationship = conceptReferenceTermRelationship;
        this.referenceTermSource = conceptReferenceTermSource;
    }

    public ConceptReferenceTerm(String conceptReferenceTermCode, String conceptReferenceTermName, String conceptReferenceTermRelationship, String conceptReferenceTermSource, String conceptReferenceDescription, String conceptReferenceVersion) {
        this.referenceTermCode = conceptReferenceTermCode;
        this.referenceTermName = conceptReferenceTermName;
        this.referenceTermRelationship = conceptReferenceTermRelationship;
        this.referenceTermSource = conceptReferenceTermSource;
        this.referenceDescription = conceptReferenceDescription;
        this.referenceVersion = conceptReferenceVersion;
    }

    public String getReferenceTermName() {
        return referenceTermName;
    }

    public void setReferenceTermName(String referenceTermName) {
        this.referenceTermName = referenceTermName;
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

    public String getReferenceTermSource() {
        return referenceTermSource;
    }

    public void setReferenceTermSource(String referenceTermSource) {
        this.referenceTermSource = referenceTermSource;
    }

    public String getReferenceDescription() {
        return referenceDescription;
    }

    public void setReferenceDescription(String referenceDescription) {
        this.referenceDescription = referenceDescription;
    }

    public String getReferenceVersion() {
        return referenceVersion;
    }

    public void setReferenceVersion(String referenceVersion) {
        this.referenceVersion = referenceVersion;
    }

}
