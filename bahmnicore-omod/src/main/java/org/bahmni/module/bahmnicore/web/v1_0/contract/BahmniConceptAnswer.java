package org.bahmni.module.bahmnicore.web.v1_0.contract;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;

public class BahmniConceptAnswer {
    private Drug drug;
    private Concept concept;

    public static BahmniConceptAnswer create(ConceptAnswer answer) {
        BahmniConceptAnswer bahmniConceptAnswer = new BahmniConceptAnswer();
        if(answer.getAnswerDrug() != null){
            bahmniConceptAnswer.setDrug(answer.getAnswerDrug());
        }
        else{
            bahmniConceptAnswer.setConcept(answer.getAnswerConcept());
        }
        return bahmniConceptAnswer;
    }

    public Drug getDrug() {
        return drug;
    }

    public void setDrug(Drug drug) {
        this.drug = drug;
    }

    public Concept getConcept() {
        return concept;
    }

    public void setConcept(Concept concept) {
        this.concept = concept;
    }

}
