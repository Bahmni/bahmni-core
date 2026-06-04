package org.bahmni.module.referencedata.labconcepts.mapper;

import org.bahmni.module.referencedata.labconcepts.contract.LabTest;
import org.bahmni.module.referencedata.labconcepts.contract.Panel;
import org.bahmni.module.referencedata.labconcepts.contract.TestsAndPanels;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;

import static org.bahmni.module.referencedata.labconcepts.contract.LabTest.LAB_TEST_CONCEPT_CLASSES;
import static org.bahmni.module.referencedata.labconcepts.mapper.ConceptExtension.*;

public class TestAndPanelMapper extends ResourceMapper {

    private final LabTestMapper labTestMapper;
    private PanelMapper panelMapper;

    public TestAndPanelMapper() {
        super(null);
        labTestMapper = new LabTestMapper();
        panelMapper = new PanelMapper();
    }

    @Override
    public TestsAndPanels map(Concept sampleConcept) {
        TestsAndPanels testsAndPanels = new TestsAndPanels();
        for (Concept concept : sampleConcept.getSetMembers()) {
            if (ConceptExtension.isActive(concept)) addConcept(testsAndPanels, concept);
        }
        return testsAndPanels;
    }

    private void addConcept(TestsAndPanels testsAndPanels, Concept concept) {
        if (isOfAnyConceptClass(concept, LAB_TEST_CONCEPT_CLASSES)) {
            LabTest test = labTestMapper.map(concept);
            testsAndPanels.addTest(test);
        } else if (isOfConceptClassByUUID(concept, ConceptClass.LABSET_UUID)) {
            Panel panel = panelMapper.map(concept);
            testsAndPanels.addPanel(panel);
        }
    }
}
