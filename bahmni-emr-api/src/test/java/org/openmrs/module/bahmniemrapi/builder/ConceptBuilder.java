package org.openmrs.module.bahmniemrapi.builder;

import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.util.LocaleUtility;

import java.util.Arrays;
import java.util.Date;

public class ConceptBuilder {
    private final Concept concept;

    public ConceptBuilder() {
        concept = new Concept();
    }

    public Concept build() {
        return concept;
    }

    public ConceptBuilder withName(String conceptName) {
        ConceptName name = new ConceptName(conceptName, LocaleUtility.getDefaultLocale());
        name.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        concept.setPreferredName(name);
        return this;
    }

    public ConceptBuilder withDataType(String name) {
        withDataType(name, "hl7Abbreviation", null);
        return this;
    }

    public ConceptBuilder withDataType(String name, String hl7Abbreviation) {
        withDataType(name, hl7Abbreviation, null);
        return this;
    }

    public ConceptBuilder withUUID(String uuid) {
        concept.setUuid(uuid);
        return this;
    }

    public ConceptBuilder withClass(String conceptClassName) {
        ConceptClass conceptClass = concept.getConceptClass();
        if (conceptClass == null) {
            conceptClass = new ConceptClass();
        }
        conceptClass.setName(conceptClassName);
        concept.setConceptClass(conceptClass);
        return this;
    }

    public ConceptBuilder withClassUUID(String uuid) {
        ConceptClass conceptClass = concept.getConceptClass();
        if (conceptClass == null) {
            conceptClass = new ConceptClass();
        }
        conceptClass.setUuid(uuid);
        concept.setConceptClass(conceptClass);
        return this;
    }


    public ConceptBuilder withSetMember(Concept setMember) {
        concept.addSetMember(setMember);
        return this;
    }

    public ConceptBuilder withDataTypeNumeric() {
        withDataType("Numeric", ConceptDatatype.NUMERIC, ConceptDatatype.NUMERIC_UUID);
        return this;
    }

    public ConceptBuilder withCodedDataType() {
        withDataType("Coded", ConceptDatatype.CODED, ConceptDatatype.CODED_UUID);
        return this;
    }

    public ConceptBuilder withDateCreated(Date dateCreated) {
        concept.setDateCreated(dateCreated);
        return this;
    }

    public ConceptBuilder withDateChanged(Date dateChanged) {
        concept.setDateChanged(dateChanged);
        return this;
    }

    public ConceptBuilder withRetired(Boolean retired) {
        concept.setRetired(retired);
        return this;
    }

    public ConceptBuilder withShortName(String name) {
        ConceptName conceptName = new ConceptName(name, Context.getLocale());
        concept.setShortName(conceptName);
        return this;
    }

    private ConceptBuilder withDataType(String name, String hl7Abbreviation, String uuid) {
        ConceptDatatype conceptDatatype = new ConceptDatatype();
        conceptDatatype.setHl7Abbreviation(hl7Abbreviation);
        conceptDatatype.setName(name);
        conceptDatatype.setUuid(uuid);
        concept.setDatatype(conceptDatatype);
        return this;
    }

    public ConceptBuilder withDescription(String description) {
        ConceptDescription conceptDescription = new ConceptDescription(description, Context.getLocale());
        concept.setDescriptions(Arrays.asList(conceptDescription));
        return this;
    }
}
