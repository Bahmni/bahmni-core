package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.Arrays;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SurgeryObsOrderLinkPreSaveCommandImplTest {

    private static final String SELECT_SURGERY_CONCEPT_UUID = "select-surgery-concept-uuid";
    private static final String ORDER_UUID_1 = "order-uuid-1";
    private static final String ORDER_UUID_2 = "order-uuid-2";
    private static final String OTHER_CONCEPT_UUID = "other-concept-uuid";
    private static final String FORM_1_PATH_PREFIX = "ENT Operative Report.1";
    private static final String FORM_2_PATH_PREFIX = "Ortho Operative Report.1";
    private static final String FORM_3_PATH_PREFIX = "General Consultation.1";

    @Mock
    private AdministrationService adminService;

    private SurgeryObsOrderLinkPreSaveCommandImpl command;

    @Before
    public void setUp() {
        command = new SurgeryObsOrderLinkPreSaveCommandImpl(adminService);
        when(adminService.getGlobalProperty(SurgeryObsOrderLinkPreSaveCommandImpl.SURGERY_SELECTION_CONCEPT_UUID_GP, ""))
                .thenReturn(SELECT_SURGERY_CONCEPT_UUID);
    }

    @Test
    public void shouldStampOrderUuidOnAllObsInFormWithSelectSurgeryObs() {
        BahmniObservation selectSurgeryObs = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation careInstructionObs = obs(OTHER_CONCEPT_UUID, "some value", FORM_1_PATH_PREFIX);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(selectSurgeryObs, careInstructionObs));

        command.update(transaction);

        assertEquals(ORDER_UUID_1, selectSurgeryObs.getOrderUuid());
        assertEquals(ORDER_UUID_1, careInstructionObs.getOrderUuid());
    }

    @Test
    public void shouldStampEachFormWithItsOwnOrderUuid() {
        BahmniObservation selectSurgeryForm1 = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation careInstructionForm1 = obs(OTHER_CONCEPT_UUID, "value", FORM_1_PATH_PREFIX);
        BahmniObservation selectSurgeryForm2 = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_2, FORM_2_PATH_PREFIX);
        BahmniObservation careInstructionForm2 = obs(OTHER_CONCEPT_UUID, "value", FORM_2_PATH_PREFIX);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(
                selectSurgeryForm1, careInstructionForm1,
                selectSurgeryForm2, careInstructionForm2));

        command.update(transaction);

        assertEquals(ORDER_UUID_1, selectSurgeryForm1.getOrderUuid());
        assertEquals(ORDER_UUID_1, careInstructionForm1.getOrderUuid());
        assertEquals(ORDER_UUID_2, selectSurgeryForm2.getOrderUuid());
        assertEquals(ORDER_UUID_2, careInstructionForm2.getOrderUuid());
    }

    @Test
    public void shouldNotStampObsInFormWithoutSelectSurgery() {
        BahmniObservation selectSurgeryForm1 = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation careInstructionForm1 = obs(OTHER_CONCEPT_UUID, "value", FORM_1_PATH_PREFIX);
        BahmniObservation nonSurgeryFormObs = obs(OTHER_CONCEPT_UUID, "value", FORM_3_PATH_PREFIX);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(
                selectSurgeryForm1, careInstructionForm1, nonSurgeryFormObs));

        command.update(transaction);

        assertEquals(ORDER_UUID_1, careInstructionForm1.getOrderUuid());
        assertNull(nonSurgeryFormObs.getOrderUuid());
    }

    @Test
    public void shouldNotOverwriteExistingOrderUuidOnObs() {
        BahmniObservation selectSurgeryObs = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation obsWithExistingOrder = obs(OTHER_CONCEPT_UUID, "value", FORM_1_PATH_PREFIX);
        obsWithExistingOrder.setOrderUuid("existing-order-uuid");

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(selectSurgeryObs, obsWithExistingOrder));

        command.update(transaction);

        assertEquals("existing-order-uuid", obsWithExistingOrder.getOrderUuid());
    }

    @Test
    public void shouldStampOrderUuidOnGroupMemberObs() {
        BahmniObservation selectSurgeryObs = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation childObs = obs(OTHER_CONCEPT_UUID, "child value", FORM_1_PATH_PREFIX);
        BahmniObservation parentObs = obs("parent-concept-uuid", null, FORM_1_PATH_PREFIX);
        parentObs.addGroupMember(childObs);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(selectSurgeryObs, parentObs));

        command.update(transaction);

        assertEquals(ORDER_UUID_1, parentObs.getOrderUuid());
        assertEquals(ORDER_UUID_1, childObs.getOrderUuid());
    }

    @Test
    public void shouldSkipVoidedObs() {
        BahmniObservation selectSurgeryObs = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation voidedObs = obs(OTHER_CONCEPT_UUID, "value", FORM_1_PATH_PREFIX);
        voidedObs.setVoided(true);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(selectSurgeryObs, voidedObs));

        command.update(transaction);

        assertNull(voidedObs.getOrderUuid());
    }

    @Test
    public void shouldSkipWhenGlobalPropertyNotConfigured() {
        when(adminService.getGlobalProperty(SurgeryObsOrderLinkPreSaveCommandImpl.SURGERY_SELECTION_CONCEPT_UUID_GP, ""))
                .thenReturn("");

        BahmniObservation selectSurgeryObs = obs(SELECT_SURGERY_CONCEPT_UUID, ORDER_UUID_1, FORM_1_PATH_PREFIX);
        BahmniObservation careInstructionObs = obs(OTHER_CONCEPT_UUID, "value", FORM_1_PATH_PREFIX);

        BahmniEncounterTransaction transaction = new BahmniEncounterTransaction();
        transaction.setObservations(Arrays.asList(selectSurgeryObs, careInstructionObs));

        command.update(transaction);

        assertNull(careInstructionObs.getOrderUuid());
    }

    private BahmniObservation obs(String conceptUuid, Object value, String formPathPrefix) {
        BahmniObservation obs = new BahmniObservation();
        EncounterTransaction.Concept concept = new EncounterTransaction.Concept();
        concept.setUuid(conceptUuid);
        obs.setConcept(concept);
        if (value != null) {
            obs.setValue(value);
        }
        obs.setFormFieldPath(formPathPrefix + "/1-0");
        obs.setConceptSortWeight(0);
        return obs;
    }
}
