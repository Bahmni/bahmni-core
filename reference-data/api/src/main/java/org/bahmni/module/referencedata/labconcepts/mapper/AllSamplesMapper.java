package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.AllSamples;
import org.openmrs.Concept;

import static org.bahmni.module.referencedata.labconcepts.contract.Sample.SAMPLE_CONCEPT_CLASSES;


public class AllSamplesMapper extends ResourceMapper {
    public AllSamplesMapper() {
        super(null);
    }

    @Override
    public AllSamples map(Concept allSamplesConcept) {
        AllSamples allSamples = new AllSamples();
        allSamples = mapResource(allSamples, allSamplesConcept);
        allSamples.setDescription(ConceptExtension.getDescription(allSamplesConcept));

        for (Concept setMember : allSamplesConcept.getSetMembers()) {
            if (ConceptExtension.isOfAnyConceptClass(setMember, SAMPLE_CONCEPT_CLASSES)) {
                SampleMapper sampleMapper = new SampleMapper();
                allSamples.addSample(sampleMapper.map(setMember));
            }
        }
        return allSamples;
    }
}
