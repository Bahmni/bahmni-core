package org.bahmni.module.referencedata.labconcepts.model.event;

import org.bahmni.test.builder.ConceptBuilder;
import org.ict4h.atomfeed.server.service.Event;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.Drug;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DrugEventTest {

    private static final Object[] WRONG_ARGUMENTS = new Object[]{};
    private Object[] drugs;
    private Drug drug;
    private Object[] notDrugs;
    private DrugEvent drugEvent;


    @Before
    public void setUp() throws Exception {
        Concept drugConcept = new ConceptBuilder().withClass("drug").withUUID("drugConceptUuid").withClassUUID(ConceptClass.DRUG_UUID).build();
        drug = new Drug();
        drug.setConcept(drugConcept);
        drug.setUuid("drugUUID");
        drugs = new Object[]{drug};
        Concept notDrug = new Concept();
        notDrugs = new Object[]{notDrug};
        drugEvent = new DrugEvent(ConceptServiceEventFactory.CONCEPT_URL, ConceptServiceEventFactory.DRUG, ConceptServiceEventFactory.DRUG);
    }


    @Test
    public void notApplicableForWrongOperation() throws Exception {
        Boolean applicable = drugEvent.isApplicable("don'tSaveDrug", WRONG_ARGUMENTS);
        assertFalse(applicable);
    }

    @Test
    public void notApplicableForNullOperation() throws Exception {
        Boolean applicable = drugEvent.isApplicable(null, WRONG_ARGUMENTS);
        assertFalse(applicable);
    }

    @Test
    public void notApplicableForNullArguments() throws Exception {
        Boolean applicable = drugEvent.isApplicable("saveDrug", null);
        assertFalse(applicable);
    }

    @Test
    public void notApplicableForWrongArguments() throws Exception {
        Boolean applicable = drugEvent.isApplicable("saveDrug", WRONG_ARGUMENTS);
        assertFalse(applicable);
    }

    @Test
    public void notApplicableForWrongArgumentType() throws Exception {
        Boolean applicable = drugEvent.isApplicable("saveDrug", notDrugs);
        assertFalse(applicable);
    }

    @Test
    public void applicableForRightOperationsAndArguments() throws Exception {
        Boolean applicable = drugEvent.isApplicable("saveDrug", drugs);
        assertTrue(applicable);
    }

    @Test
    public void publishEventForDrugs() throws Exception {
        Event event = drugEvent.asAtomFeedEvent(drugs);
        assertEquals(ConceptServiceEventFactory.DRUG, event.getCategory());
        assertEquals(ConceptServiceEventFactory.DRUG, event.getTitle());
        assertEquals(String.format(ConceptServiceEventFactory.CONCEPT_URL, event.getCategory(), drug.getUuid()), event.getUri().toString());
    }
}