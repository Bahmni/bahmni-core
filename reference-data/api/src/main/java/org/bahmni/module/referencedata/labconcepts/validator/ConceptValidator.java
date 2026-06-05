package org.bahmni.module.referencedata.labconcepts.validator;

import org.bahmni.module.referencedata.labconcepts.contract.Concept;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptCommon;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;

import java.util.ArrayList;
import java.util.List;

public class ConceptValidator extends Validator {

    public void validate(Concept conceptData, ConceptClass conceptClassName, ConceptDatatype conceptDatatype, List<String> notFound) {
        List<String> errors = validateConceptCommon(conceptData, conceptClassName, conceptDatatype, notFound);
        if (conceptDatatype != null && !conceptDatatype.isCoded() && hasAnswers(conceptData)) {
            errors.add("Cannot create answers for concept " + conceptData.getUniqueName() + " having datatype " + conceptData.getDataType());
        }
        throwExceptionIfExists(errors);
    }

    public void validate(ConceptSet conceptSet, ConceptClass conceptClass, ConceptDatatype conceptDatatype, List<String> notFound) {
        List<String> errors = validateConceptCommon(conceptSet, conceptClass, conceptDatatype, notFound);
        throwExceptionIfExists(errors);
    }

    private List<String> validateConceptCommon(ConceptCommon conceptData, ConceptClass conceptClassName, ConceptDatatype conceptDatatype, List<String> notFound) {
        List<String> errors = new ArrayList<>();
        if (conceptClassName == null) {
            errors.add("Concept Class " + conceptData.getClassName() + " not found");
        }
        if (conceptDatatype == null) {
            errors.add("Concept Datatype " + conceptData.getDataType() + " not found");
        }
        for (String notFoundItem : notFound) {
            errors.add(notFoundItem + " Concept/ConceptAnswer not found");
        }
        return errors;
    }


    private boolean hasAnswers(Concept conceptData) {
        return conceptData.getAnswers() != null && conceptData.getAnswers().size() > 0;
    }
}
