package org.openmrs.module.bahmniemrapi.encountertransaction.contract;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.ETObsToBahmniObsMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.mapper.parameters.AdditionalBahmniObservationFields;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class BahmniObservationTest {
    private EncounterTransaction.Observation eTObservation;

    @Mock
    private ConceptService conceptService;

    @Mock
    private User authenticatedUser;

    @Before
    public void setUp() throws Exception {
        eTObservation = new EncounterTransaction.Observation();
        initMocks(this);
        mockStatic(Context.class);
        Mockito.when(Context.getAuthenticatedUser()).thenReturn(authenticatedUser);
    }

    @Test
    public void shouldCreateBahmniObservationFromETObservation(){
        Date obsDate = new Date();
        EncounterTransaction.Concept concept = createConcept("concept-uuid", "concept-name");
        Concept conceptFromService = PowerMockito.mock(Concept.class);
        conceptFromService.setUuid("concept-uuid");
        ConceptName conceptNameFromService = new ConceptName();
        conceptNameFromService.setName("concept-name");

        when(conceptFromService.getName()).thenReturn(conceptNameFromService);
        when(conceptService.getConceptByUuid("concept-uuid")).thenReturn(conceptFromService);

        eTObservation = createETObservation("obs-uuid", "obs-value", concept, obsDate);

        eTObservation.addGroupMember(createETObservation("child-uuid", "child-value", concept, obsDate));

        BahmniObservation observation =  new ETObsToBahmniObsMapper(conceptService, Arrays.asList()).create(eTObservation, new AdditionalBahmniObservationFields("encounter-uuid",new Date(),null,"obs-Group-Uuid"));
        assertEquals("comment", observation.getComment());
        assertEquals("obs-uuid", observation.getUuid());
        assertEquals("concept-uuid",observation.getConceptUuid());
        assertEquals("order-uuid", observation.getOrderUuid());
        assertEquals(obsDate,observation.getObservationDateTime());
        Collection<BahmniObservation> groupMembers = observation.getGroupMembers();
        assertEquals(1, groupMembers.size());
        assertEquals("obs-value",observation.getValue());
        assertEquals(true, observation.getVoided());
        assertEquals("void reason", observation.getVoidReason());
        assertEquals("encounter-uuid",observation.getEncounterUuid());
        assertEquals("obs-Group-Uuid",observation.getObsGroupUuid());

        BahmniObservation child = groupMembers.iterator().next();
        assertEquals("child-uuid", child.getUuid());
        assertEquals("child-value", child.getValue());
        assertEquals("encounter-uuid",child.getEncounterUuid());
    }

    @Test
    public void shouldReturnTrueIfBahmniObservationIsSameAsETObservation() throws Exception {
        eTObservation.setUuid("uuid");
        BahmniObservation bahmniObservation = new BahmniObservation();
        bahmniObservation.setUuid("uuid");

        boolean isSame = bahmniObservation.isSameAs(eTObservation);

        Assert.assertTrue(isSame);
    }

    @Test
    public void shouldConvertBahmniObservationToETObservation() throws Exception {
        Date obsDateTime = new Date();
        EncounterTransaction.Concept concept = createConcept("concept-uuid", "concept-name");
        BahmniObservation bahmniObservation = createBahmniObservation("obs-uuid", "obs-value", concept, obsDateTime, "parentConceptUuid");
        bahmniObservation.addGroupMember(createBahmniObservation("child-uuid", "child-value", concept, obsDateTime, "parentConceptUuid"));

        EncounterTransaction.Observation observation = bahmniObservation.toETObservation();
        
        assertEquals("comment",observation.getComment());
        assertEquals("obs-uuid",observation.getUuid());
        assertEquals("concept-uuid",observation.getConceptUuid());
        assertEquals("order-uuid",observation.getOrderUuid());
        assertEquals(obsDateTime,observation.getObservationDateTime());
        assertEquals(1,observation.getGroupMembers().size());
        assertEquals("obs-value",observation.getValue());
        assertEquals(true,observation.getVoided());
        assertEquals("void reason", observation.getVoidReason());
        assertEquals("child-uuid", observation.getGroupMembers().get(0).getUuid());
        assertEquals("child-value", observation.getGroupMembers().get(0).getValue());
        assertEquals("formUuid", observation.getFormNamespace());
        assertEquals("formFieldPath", observation.getFormFieldPath());
    }

    @Test
    public void testBahmniObservationCreation() {
        Date obsDateTime = new Date();
        EncounterTransaction.Concept concept = createConcept("concept-uuid", "concept-name");
        BahmniObservation bahmniObservation = createBahmniObservation("obs-uuid", "obs-value", concept, obsDateTime, "parentConceptUuid");

        assertEquals("concept-name", bahmniObservation.getConceptNameToDisplay());
        assertEquals("formUuid", bahmniObservation.getFormNamespace());
        assertEquals("formFieldPath", bahmniObservation.getFormFieldPath());
    }

    private EncounterTransaction.Concept createConcept(String conceptUuid, String conceptName) {
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        concept.setName(conceptName);
        return concept;
    }

    private BahmniObservation createBahmniObservation(String uuid,String value,EncounterTransaction.Concept concept,Date obsDate, String parentConceptUuid) {
        BahmniObservation bahmniObservation1 = new BahmniObservation();
        bahmniObservation1.setUuid(uuid);
        bahmniObservation1.setValue(value);
        bahmniObservation1.setConcept(concept);
        bahmniObservation1.setComment("comment");
        bahmniObservation1.setObservationDateTime(obsDate);
        bahmniObservation1.setOrderUuid("order-uuid");
        bahmniObservation1.setVoided(true);
        bahmniObservation1.setVoidReason("void reason");
        bahmniObservation1.setParentConceptUuid(parentConceptUuid);
        bahmniObservation1.setFormNamespace("formUuid");
        bahmniObservation1.setFormFieldPath("formFieldPath");
        return bahmniObservation1;
    }

    private EncounterTransaction.Observation createETObservation(String uuid,String value,EncounterTransaction.Concept concept,final Date obsDate) {
        EncounterTransaction.Observation etObservation = new EncounterTransaction.Observation();
        etObservation.setUuid(uuid);
        etObservation.setValue(value);
        etObservation.setConcept(concept);
        etObservation.setComment("comment");
        etObservation.setObservationDateTime(obsDate);
        etObservation.setOrderUuid("order-uuid");
        etObservation.setVoided(true);
        etObservation.setVoidReason("void reason");
        return etObservation;
    }
}
