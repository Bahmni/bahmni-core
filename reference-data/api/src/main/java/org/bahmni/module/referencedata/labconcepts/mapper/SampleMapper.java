package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.contract.Sample;
import org.openmrs.Concept;
import org.openmrs.api.context.Context;

import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.getResourceReferencesOfConceptClass;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.getResourceReferencesOfConceptClasses;

public class SampleMapper extends ResourceMapper {
    public SampleMapper() {
        super(AllSamples.ALL_SAMPLES);
    }

    @Override
    public Sample map(Concept sampleConcept) {
        Sample sample = new Sample();
        sample = mapResource(sample, sampleConcept);
        sample.setShortName(sampleConcept.getShortestName(Context.getLocale(), false).getName());
        sample.setSortOrder(getSortWeight(sampleConcept));
        sample.setTests(getResourceReferencesOfConceptClasses(sampleConcept.getSetMembers(), LabTest.LAB_TEST_CONCEPT_CLASSES));
        sample.setPanels(getResourceReferencesOfConceptClass(sampleConcept.getSetMembers(), Panel.LAB_SET_CONCEPT_CLASS));
        return sample;
    }

}
