package org.bahmni.module.referencedata.labconcepts.mapper;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;

import java.util.Collection;
import java.util.List;

public class ConceptAnswerMapper {

    public Concept map(Concept concept, List<ConceptAnswer> answers) {
        double sortWeight = 0.0;
        removeConceptAnswers(concept);
        for (ConceptAnswer answer : answers) {
            sortWeight++;
            addAnswer(concept, answer, sortWeight);
        }
        return concept;
    }

    private org.openmrs.Concept addAnswer(org.openmrs.Concept concept, ConceptAnswer answer, double sortWeight) {
        for (ConceptAnswer conceptAnswer : concept.getAnswers()) {
            if (conceptAnswer.getAnswerConcept().getName(Context.getLocale()).getName().equals(answer.getAnswerConcept().getName(Context.getLocale()).getName())) {
                return concept;
            }
        }
        answer.setSortWeight(sortWeight);
        concept.addAnswer(answer);
        return concept;
    }

    private void removeConceptAnswers(org.openmrs.Concept concept) {
        Collection<ConceptAnswer> answers = concept.getAnswers();
        answers.clear();
        concept.setAnswers(answers);
    }

}
