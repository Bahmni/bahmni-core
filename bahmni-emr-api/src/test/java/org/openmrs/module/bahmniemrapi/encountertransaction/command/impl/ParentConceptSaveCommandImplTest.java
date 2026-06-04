package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;

public class ParentConceptSaveCommandImplTest {

    @Test
    public void ensureBahmniObsIsUpdatedWithParentConceptUuid() throws Exception {

        EncounterTransaction.Concept vitals = new EncounterTransaction.Concept();
        vitals.setUuid("vitals");

        EncounterTransaction.Concept height = new EncounterTransaction.Concept();
        height.setUuid("height");

        BahmniObservation heightObs = new BahmniObservation();
        heightObs.setUuid("heightUuid");
        heightObs.setConcept(height);

        BahmniObservation vitalsObs = new BahmniObservation();
        vitalsObs.setUuid("parentUuid");
        vitalsObs.setConcept(vitals);
        vitalsObs.setGroupMembers(Arrays.asList(heightObs));

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setObservations(Arrays.asList(vitalsObs));

        ParentConceptSaveCommandImpl updateConcept = new ParentConceptSaveCommandImpl();
        bahmniEncounterTransaction = updateConcept.update(bahmniEncounterTransaction);

        assertEquals(1,bahmniEncounterTransaction.getObservations().size());
        BahmniObservation actualObs = bahmniEncounterTransaction.getObservations().iterator().next();
        assertEquals("vitals",actualObs.getParentConceptUuid());
        assertEquals("vitals",actualObs.getGroupMembers().iterator().next().getParentConceptUuid()); //Height
    }
}