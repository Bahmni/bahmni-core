package org.bahmni.module.referencedata.contract;

public class ConceptName {
    private String fullySpecifiedName;
    private String shortName;

    public ConceptName(String fullySpecifiedName, String shortName) {
        this.fullySpecifiedName = fullySpecifiedName;
        this.shortName = shortName;
    }

    public ConceptName() {

    }

    public String getFullySpecifiedName() {
        return fullySpecifiedName;
    }

    public void setFullySpecifiedName(String fullySpecifiedName) {
        this.fullySpecifiedName = fullySpecifiedName;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ConceptName that = (ConceptName) o;

        if (fullySpecifiedName != null ? !fullySpecifiedName.equals(that.fullySpecifiedName) : that.fullySpecifiedName != null)
            return false;
        return shortName != null ? shortName.equals(that.shortName) : that.shortName == null;
    }

    @Override
    public int hashCode() {
        int result = fullySpecifiedName != null ? fullySpecifiedName.hashCode() : 0;
        result = 31 * result + (shortName != null ? shortName.hashCode() : 0);
        return result;
    }
}
