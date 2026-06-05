package org.bahmni.module.bahmnicore.customdatatype.datatype;

import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.InvalidCustomValueException;
import org.openmrs.customdatatype.SerializingCustomDatatype;

import java.util.Collection;

public class CodedConceptDatatype extends SerializingCustomDatatype<Concept> {
    private Concept codedConcept;

    @Override
    public void setConfiguration(String id) {
        this.codedConcept = Context.getConceptService().getConcept(Integer.valueOf(id));
    }

    @Override
    public String serialize(Concept concept) {
        return concept.getId().toString();
    }

    @Override
    public Concept deserialize(String value) {
        try {
            return Context.getConceptService().getConcept(Integer.valueOf(value));
        } catch (NumberFormatException e) {
            return Context.getConceptService().getConceptByUuid(value);
        }
    }

    @Override
    public void validate(Concept concept) throws InvalidCustomValueException {
        Collection<ConceptAnswer> answers = codedConcept.getAnswers();
        for (ConceptAnswer existingAnswer : answers) {
            if (existingAnswer.getAnswerConcept().equals(concept))
                return;
        }
        throw new InvalidCustomValueException("Doesn't match the Coded Concept Answers");
    }
}
