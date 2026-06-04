package org.bahmni.test.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptName;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;

public class ConceptNumericBuilder {
    private final org.openmrs.ConceptNumeric concept;

    public ConceptNumericBuilder() {
        concept = new ConceptNumeric();
        withDataType("Numeric", "Abb", "dataTypeUuid");
    }

    public Concept build() {
        return concept;
    }

    public ConceptNumericBuilder withName(String conceptName) {
        ConceptName name = new ConceptName(conceptName, LocaleUtility.getDefaultLocale());
        name.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        concept.setPreferredName(name);
        return this;
    }

    public ConceptNumericBuilder withLowNormal(Double lowNormal){
        concept.setLowNormal(lowNormal);
        return this;
    }


    public ConceptNumericBuilder withHiNormal(Double hiNormal){
        concept.setHiNormal(hiNormal);
        return this;
    }

    public ConceptNumericBuilder withClass(String className) {
        ConceptClass conceptClass = concept.getConceptClass();
        if (conceptClass == null) {
            conceptClass = new ConceptClass();
        }
        conceptClass.setName(className);
        concept.setConceptClass(conceptClass);
        return this;
    }

    public ConceptNumericBuilder withSetMember(Concept childConcept) {
        concept.addSetMember(childConcept);
        return this;
    }

    public ConceptNumericBuilder withUnit(String unit) {
        concept.setUnits(unit);
        return this;
    }

    private ConceptNumericBuilder withDataType(String name, String hl7Abbreviation, String uuid) {
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setHl7Abbreviation(hl7Abbreviation);
        conceptDatatype.setName(name);
        conceptDatatype.setUuid(uuid);
        concept.setDatatype(conceptDatatype);
        return this;
    }

    public ConceptNumericBuilder withId(Integer id) {
        concept.setId(id);
        return this;
    }

    public ConceptNumericBuilder withShortName(String name) {
        concept.setShortName(name != null ? new ConceptName(name, Context.getLocale()) : null);
        return this;
    }

    public ConceptNumericBuilder withRetired(boolean retired) {
        concept.setRetired(retired);
        return this;
    }

}
