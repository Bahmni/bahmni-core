package org.openmrs.module.bahmniemrapi.encountertransaction.mapper;

import org.bahmni.module.obsrelationship.api.ObsRelationService;
import org.bahmni.module.obsrelationship.model.ObsRelationship;
import org.bahmni.module.obsrelationship.model.ObsRelationshipType;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.EncounterProviderMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PrepareForTest(LocaleUtility.class)
@RunWith(PowerMockRunner.class)
public class ObsRelationshipMapperTest {

    @Mock
    private ObsRelationService obsrelationService;
    @Mock
    private EncounterProviderMapper encounterProviderMapper;

    private ObsRelationshipMapper obsRelationshipMapper;

    @Mock
    private OMRSObsToBahmniObsMapper OMRSObsToBahmniObsMapper;

    @Before
    public void setUp() throws Exception {
        PowerMockito.mockStatic(LocaleUtility.class);
        PowerMockito.when(LocaleUtility.getLocalesInOrder()).thenReturn(new HashSet<>(Arrays.asList(Locale.getDefault())));

        initMocks(this);
        obsRelationshipMapper = new ObsRelationshipMapper(obsrelationService, encounterProviderMapper, OMRSObsToBahmniObsMapper);
    }

    @Test
    public void shouldMapObsRelationshipForBahmniObservations() {
        String sourceObsUuid = "source-obs-uuid";
        String targetObsUuid = "target-obs-uuid";

        Obs sourceObs = createObs(sourceObsUuid);
        Obs targetObs = createObs(targetObsUuid);

        List<ObsRelationship> obsRelationShips = new ArrayList<>();
        obsRelationShips.add(createObsRelationship(sourceObs, targetObs));


        when(obsrelationService.getRelationsWhereSourceObsInEncounter("encounter-uuid")).thenReturn(obsRelationShips);

        BahmniObservation sourceObservation = getBahmniObservation(sourceObsUuid);
        BahmniObservation targetObservation = getBahmniObservation(targetObsUuid);

        when(OMRSObsToBahmniObsMapper.map(targetObs, null)).thenReturn(targetObservation);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(sourceObservation);
        bahmniObservations.add(targetObservation);

        List<BahmniObservation> mappedBahmniObservations = obsRelationshipMapper.map(bahmniObservations, "encounter-uuid");
        
        verify(obsrelationService).getRelationsWhereSourceObsInEncounter("encounter-uuid");
        verify(OMRSObsToBahmniObsMapper, times(1)).map(targetObs, null);
        assertEquals(2, mappedBahmniObservations.size());
        assertEquals(sourceObsUuid, mappedBahmniObservations.get(0).getUuid());
        assertEquals(targetObsUuid, mappedBahmniObservations.get(0).getTargetObsRelation().getTargetObs().getUuid());
        assertEquals("obsRelationType", mappedBahmniObservations.get(0).getTargetObsRelation().getRelationshipType());
    }

    @Test
    public void shouldMapMultipleObsRelationshipForBahmniObservations() {
        String sourceObs1Uuid = "source1-obs-uuid";
        String targetObs1Uuid = "target1-obs-uuid";

        String sourceObs2Uuid = "source2-obs-uuid";
        String targetObs2Uuid = "target2-obs-uuid";

        Obs sourceObs1 = createObs(sourceObs1Uuid);
        Obs sourceObs2 = createObs(sourceObs2Uuid);
        Obs targetObs1 = createObs(targetObs1Uuid);
        Obs targetObs2 = createObs(targetObs2Uuid);

        List<ObsRelationship> obsRelationShips = new ArrayList<>();
        obsRelationShips.add(createObsRelationship(sourceObs1, targetObs1));
        obsRelationShips.add(createObsRelationship(sourceObs2, targetObs2));

        when(obsrelationService.getRelationsWhereSourceObsInEncounter("encounter-uuid")).thenReturn(obsRelationShips);

        BahmniObservation sourceObservation1 = getBahmniObservation(sourceObs1Uuid);
        BahmniObservation sourceObservation2 = getBahmniObservation(sourceObs2Uuid);
        BahmniObservation targetObservation1 = getBahmniObservation(targetObs1Uuid);
        BahmniObservation targetObservation2 = getBahmniObservation(targetObs2Uuid);

        when(OMRSObsToBahmniObsMapper.map(targetObs1, null)).thenReturn(targetObservation1);
        when(OMRSObsToBahmniObsMapper.map(targetObs2, null)).thenReturn(targetObservation2);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(sourceObservation1);
        bahmniObservations.add(sourceObservation2);
        bahmniObservations.add(targetObservation1);
        bahmniObservations.add(targetObservation2);

        List<BahmniObservation> mappedBahmniObservations = obsRelationshipMapper.map(bahmniObservations, "encounter-uuid");

        verify(obsrelationService).getRelationsWhereSourceObsInEncounter("encounter-uuid");
        verify(OMRSObsToBahmniObsMapper, times(2)).map(any(Obs.class), any());
        assertEquals(4, mappedBahmniObservations.size());
        assertEquals(sourceObs1Uuid, mappedBahmniObservations.get(0).getUuid());
        assertEquals(targetObs1Uuid, mappedBahmniObservations.get(0).getTargetObsRelation().getTargetObs().getUuid());
        assertEquals(sourceObs2Uuid, mappedBahmniObservations.get(1).getUuid());
        assertEquals(targetObs2Uuid, mappedBahmniObservations.get(1).getTargetObsRelation().getTargetObs().getUuid());
        assertEquals("obsRelationType", mappedBahmniObservations.get(0).getTargetObsRelation().getRelationshipType());
        assertEquals("obsRelationType", mappedBahmniObservations.get(1).getTargetObsRelation().getRelationshipType());
    }

    private BahmniObservation getBahmniObservation(String sourceObsUuid) {
        BahmniObservation sourceObservation = new BahmniObservation();
        sourceObservation.setUuid(sourceObsUuid);
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept("random-uuid", "Random Concept");
        sourceObservation.setConcept(concept);
        return sourceObservation;
    }

    private ObsRelationship createObsRelationship(Obs sourceObs, Obs targetObs) {
        ObsRelationshipType obsRelationshipType = new ObsRelationshipType();
        obsRelationshipType.setName("obsRelationType");

        ObsRelationship obsRelationship = new ObsRelationship();
        obsRelationship.setObsRelationshipType(obsRelationshipType);
        obsRelationship.setSourceObs(sourceObs);
        obsRelationship.setTargetObs(targetObs);
        return obsRelationship;
    }

    private Obs createObs(String obsUuid) {
        Obs sourceObs = new Obs();
        sourceObs.setUuid(obsUuid);
        Concept concept = new Concept();
        concept.setFullySpecifiedName(new ConceptName("Random Concept", Locale.ENGLISH));
        sourceObs.setConcept(concept);
        Encounter encounter = new Encounter();
        encounter.setUuid("encounterUuid");
        sourceObs.setEncounter(encounter);
        return sourceObs;
    }
}
