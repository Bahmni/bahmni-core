package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.obsrelation.contract.ObsRelationship;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniObservationSaveCommandImplTest {
    @Mock
    private ObsService obsService;
    @Mock
    private ObsRelationService obsRelationService;

    private BahmniObservationSaveCommandImpl bahmniObservationSaveCommand;
    @Before
    public void setUp() throws Exception {
        initMocks(this);
        bahmniObservationSaveCommand = new BahmniObservationSaveCommandImpl(obsRelationService, obsService);
    }

    @Test
    public void shouldSaveObsRelationsForTheGivenObservations(){
        Date obsDate = new Date();
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        ObsRelationship targetObs = createObsRelationShip("relationTypeName", createBahmniObservation("target-uuid", "target-value", createConcept("target-concept-uuid", "target-concept-name"), obsDate, null));
        BahmniObservation srcObs = createBahmniObservation("obs-uuid", "obs-value", createConcept("concept-uuid", "concept-name"), obsDate, targetObs);
        bahmniObservations.add(srcObs);
        BahmniEncounterTransaction bahmniEncounterTransaction = createBahmniEncounterTransaction(bahmniObservations);

        Encounter currentEncounter = new Encounter();
        Set<Obs> obsList = new HashSet<>();
        obsList.add(createObs("obs-uuid","obs-value", obsDate));
        obsList.add(createObs("obs-uuid2","obs-value", obsDate));
        obsList.add(createObs("target-uuid", "target-value", obsDate));
        currentEncounter.setObs(obsList);

        ObsRelationshipType obsRelationshipType = new ObsRelationshipType();
        obsRelationshipType.setName("relationTypeName");
        when(obsRelationService.getRelationshipTypeByName(anyString())).thenReturn(obsRelationshipType);

        bahmniObservationSaveCommand.save(bahmniEncounterTransaction, currentEncounter, null);

//        verify(obsService).getObsByUuid("target-uuid");
        ArgumentCaptor<org.bahmni.module.obsrelationship.model.ObsRelationship> obsRelationshipArgument = ArgumentCaptor.forClass(org.bahmni.module.obsrelationship.model.ObsRelationship.class);
        verify(obsRelationService).saveOrUpdate(obsRelationshipArgument.capture());

        assertEquals("obs-uuid",obsRelationshipArgument.getValue().getSourceObs().getUuid());
        assertEquals("obs-uuid",obsRelationshipArgument.getValue().getSourceObs().getUuid());
        assertEquals(obsDate,obsRelationshipArgument.getValue().getSourceObs().getObsDatetime());

        assertEquals("target-uuid",obsRelationshipArgument.getValue().getTargetObs().getUuid());
        assertEquals("target-value",obsRelationshipArgument.getValue().getTargetObs().getValueText());
        assertEquals(obsDate,obsRelationshipArgument.getValue().getTargetObs().getObsDatetime());

        assertEquals("relationTypeName",obsRelationshipArgument.getValue().getObsRelationshipType().getName());
    }

    @Test
    public void shouldSaveObsRelationsWhenTargetObsNotInCurrentEncounter(){
        Date obsDate = new Date();
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        ObsRelationship targetObs = createObsRelationShip("relationTypeName", createBahmniObservation("target-uuid", "target-value", createConcept("target-concept-uuid", "target-concept-name"), obsDate, null));
        BahmniObservation srcObs = createBahmniObservation("obs-uuid", "obs-value", createConcept("concept-uuid", "concept-name"), obsDate, targetObs);
        bahmniObservations.add(srcObs);
        BahmniEncounterTransaction bahmniEncounterTransaction = createBahmniEncounterTransaction(bahmniObservations);

        Encounter currentEncounter = new Encounter();
        Set<Obs> obsList = new HashSet<>();
        obsList.add(createObs("obs-uuid","obs-value", obsDate));
        obsList.add(createObs("obs-uuid2","obs-value", obsDate));
        Obs targetObsOpenmrs = createObs("target-uuid", "target-value", obsDate);
        currentEncounter.setObs(obsList);

        ObsRelationshipType obsRelationshipType = new ObsRelationshipType();
        obsRelationshipType.setName("relationTypeName");
        when(obsService.getObsByUuid("target-uuid")).thenReturn(targetObsOpenmrs);
        when(obsRelationService.getRelationshipTypeByName(anyString())).thenReturn(obsRelationshipType);

        bahmniObservationSaveCommand.save(bahmniEncounterTransaction, currentEncounter, null);

        verify(obsService).getObsByUuid("target-uuid");
        ArgumentCaptor<org.bahmni.module.obsrelationship.model.ObsRelationship> obsRelationshipArgument = ArgumentCaptor.forClass(org.bahmni.module.obsrelationship.model.ObsRelationship.class);
        verify(obsRelationService).saveOrUpdate(obsRelationshipArgument.capture());

        assertEquals("obs-uuid",obsRelationshipArgument.getValue().getSourceObs().getUuid());
        assertEquals("obs-uuid",obsRelationshipArgument.getValue().getSourceObs().getUuid());
        assertEquals(obsDate,obsRelationshipArgument.getValue().getSourceObs().getObsDatetime());

        assertEquals("target-uuid",obsRelationshipArgument.getValue().getTargetObs().getUuid());
        assertEquals("target-value",obsRelationshipArgument.getValue().getTargetObs().getValueText());
        assertEquals(obsDate,obsRelationshipArgument.getValue().getTargetObs().getObsDatetime());

        assertEquals("relationTypeName",obsRelationshipArgument.getValue().getObsRelationshipType().getName());
    }

    private Obs createObs(String uuid, String value, Date obsDate) {
        Obs obs = new Obs();
        obs.setUuid(uuid);
        obs.setValueText(value);
        obs.setConcept(new Concept(1));
        obs.setObsDatetime(obsDate);
        return obs;
    }

    private ObsRelationship createObsRelationShip(String relationTypeName, BahmniObservation bahmniObservation) {
        ObsRelationship obsRelationship = new ObsRelationship();
        obsRelationship.setRelationshipType(relationTypeName);
        obsRelationship.setTargetObs(bahmniObservation);
        return obsRelationship;
    }

    private BahmniEncounterTransaction createBahmniEncounterTransaction(List<BahmniObservation> bahmniObservations) {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setObservations(bahmniObservations);
        return bahmniEncounterTransaction;
    }


    private EncounterTransaction.Concept createConcept(String conceptUuid, String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        concept.setName(conceptName);
        return concept;
    }

    private BahmniObservation createBahmniObservation(String uuid, String value, EncounterTransaction.Concept concept, Date obsDate, ObsRelationship targetObs) {
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setUuid(uuid);
        bahmniObservation.setValue(value);
        bahmniObservation.setConcept(concept);
        bahmniObservation.setComment("comment");
        bahmniObservation.setObservationDateTime(obsDate);
        bahmniObservation.setOrderUuid("order-uuid");
        bahmniObservation.setVoided(true);
        bahmniObservation.setVoidReason("chumma");
        bahmniObservation.setTargetObsRelation(targetObs);
        return bahmniObservation;
    }

}
