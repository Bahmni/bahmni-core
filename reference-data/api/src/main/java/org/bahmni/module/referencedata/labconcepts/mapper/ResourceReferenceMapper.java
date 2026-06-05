package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.ResourceReference;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

public class ResourceReferenceMapper {
    public ResourceReference map(Concept concept) {
        return new ResourceReference(concept.getUuid(), concept.getName(Context.getLocale()).getName());
    }
}
