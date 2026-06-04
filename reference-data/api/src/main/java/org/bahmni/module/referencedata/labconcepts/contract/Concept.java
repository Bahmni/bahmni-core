package org.bahmni.module.referencedata.labconcepts.contract;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Concept extends ConceptCommon {
    private List<String> answers;
    private List<String> synonyms;
    private String units;
    private String hiNormal;
    private String lowNormal;
    private String allowDecimal;

    public Concept() {
    }

    public Concept(String uuid, String name, String conceptDescription, String conceptClass, String conceptShortname,
                   List<ConceptReferenceTerm> conceptReferenceTermList, List<String> conceptSynonyms, List<String> conceptAnswers, String datatype) {
        super(uuid, name, conceptDescription, conceptClass, conceptShortname, conceptReferenceTermList, datatype);
        this.answers = conceptAnswers;
        this.synonyms = conceptSynonyms;
    }

    public List<String> getAnswers() {
        return answers;
    }

    public void setAnswers(List<String> answers) {
        this.answers = answers;
    }

    public List<String> getSynonyms() {
        return synonyms == null ? new ArrayList<String>() : synonyms;
    }

    public void setSynonyms(List<String> synonyms) {
        this.synonyms = synonyms;
    }


    public String getUnits() {
        return units;
    }

    public void setUnits(String units) {
        this.units = units;
    }

    public void setHiNormal(String hiNormal) {
        this.hiNormal = hiNormal;
    }

    public String getHiNormal() {
        return hiNormal;
    }

    public void setLowNormal(String lowNormal) {
        this.lowNormal = lowNormal;
    }

    public String getLowNormal() {
        return lowNormal;
    }

    public String getAllowDecimal() {
        return allowDecimal;
    }

    public void setAllowDecimal(String allowDecimal) {
        this.allowDecimal = allowDecimal;
    }
}