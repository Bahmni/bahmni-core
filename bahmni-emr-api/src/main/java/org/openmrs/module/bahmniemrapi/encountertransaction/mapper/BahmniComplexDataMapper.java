package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.openmrs.Concept;
import org.openmrs.obs.ComplexData;

import java.io.Serializable;

public interface BahmniComplexDataMapper {
    Serializable map(ComplexData complexData);
    boolean canHandle(final Concept concept, ComplexData complexData);
}
