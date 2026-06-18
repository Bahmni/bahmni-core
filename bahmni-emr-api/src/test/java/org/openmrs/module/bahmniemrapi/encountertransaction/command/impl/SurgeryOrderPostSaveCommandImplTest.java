package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.openmrs.module.bahmniemrapi.dao.SurgeryObsOrderLinkDao;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Obs;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.OrderService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.operationtheater.api.model.SurgicalAppointment;
import org.openmrs.module.operationtheater.api.service.SurgicalAppointmentService;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class, OpenmrsUtil.class})
@RunWith(PowerMockRunner.class)
public class SurgeryOrderPostSaveCommandImplTest {

    @Mock private OrderService orderService;
    @Mock private ConceptService conceptService;
    @Mock private SurgeryObsOrderLinkDao surgeryObsOrderLinkDao;
    @Mock private AdministrationService adminService;
    @Mock private SurgicalAppointmentService surgicalAppointmentService;

    private SurgeryOrderPostSaveCommandImpl command;
    private Concept selectSurgeryConcept;

    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(OpenmrsUtil.class);
        mockStatic(Context.class);
        command = new SurgeryOrderPostSaveCommandImpl(orderService, conceptService, surgeryObsOrderLinkDao, adminService);

        // GP configured with a test UUID
        String testConceptUuid = "test-concept-uuid-123";
        when(adminService.getGlobalProperty(
            SurgeryOrderPostSaveCommandImpl.SURGERY_SELECTION_CONCEPT_UUID_GP, ""))
            .thenReturn(testConceptUuid);

        // Shared concept instance — obs detection and order creation both use this
        selectSurgeryConcept = PowerMockito.mock(Concept.class);
        when(conceptService.getConceptByUuid(testConceptUuid)).thenReturn(selectSurgeryConcept);
    }

    @Test
    public void shouldCreateSurgeryOrderWhenSelectSurgeryObsPresent() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        OrderType surgeryOrderType = orderTypeWithName("Surgery Order");
        when(orderService.getOrderTypeByName("Surgery Order")).thenReturn(surgeryOrderType);
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        SurgicalAppointment appt = new SurgicalAppointment();
        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-123")).thenReturn(appt);

        Encounter encounter = encounterWithProvider();
        encounter.addObs(selectSurgeryObs("appt-uuid-123"));

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService).getOrderTypeByName("Surgery Order");
        verify(orderService).saveOrder(any(Order.class), eq(null));
        verify(surgeryObsOrderLinkDao).assignOrderToUnlinkedObs(any(Order.class), any(Encounter.class));
        verify(surgicalAppointmentService).save(appt);
    }

    @Test
    public void shouldCreateGeneralOrderWhenNoSelectSurgeryObs() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        when(orderService.getOrderTypeByName("General Order")).thenReturn(orderTypeWithName("General Order"));
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        command.save(bet, encounterWithProvider(), new EncounterTransaction());

        verify(orderService).getOrderTypeByName("General Order");
        verify(orderService, never()).getOrderTypeByName("Surgery Order");
        verify(surgicalAppointmentService, never()).getSurgicalAppointmentByUuid(anyString());
    }

    @Test
    public void shouldCreateGeneralOrderWhenSelectSurgeryObsAlreadyLinkedToExistingOrder() {
        // Simulates Form 2 (non-surgery) submitted on same encounter as Form 1 (surgery).
        // The "Select Surgery" obs from Form 1 already has an order linked — it must be ignored.
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        when(orderService.getOrderTypeByName("General Order")).thenReturn(orderTypeWithName("General Order"));
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        Encounter encounter = encounterWithProvider();
        // "Select Surgery" obs from Form 1 already has an order — should be ignored
        Obs alreadyLinkedSelectSurgeryObs = selectSurgeryObs("old-appt-uuid");
        alreadyLinkedSelectSurgeryObs.setOrder(existingOrderOfType("Surgery Order"));
        encounter.addObs(alreadyLinkedSelectSurgeryObs);

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService).getOrderTypeByName("General Order");
        verify(orderService, never()).getOrderTypeByName("Surgery Order");
    }

    @Test
    public void shouldReuseExistingSurgeryOrderWhenSameSurgicalAppointmentSubmitsAgain() {
        // Same surgical appointment submitting a second operative report — must reuse existing Surgery Order
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        Order existingSurgeryOrder = existingOrderOfType("Surgery Order");
        SurgicalAppointment appt = new SurgicalAppointment();
        appt.setOrder(existingSurgeryOrder);

        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-123")).thenReturn(appt);

        Encounter encounter = encounterWithProvider();
        encounter.addObs(selectSurgeryObs("appt-uuid-123"));

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService, never()).saveOrder(any(Order.class), any());
        verify(surgeryObsOrderLinkDao).assignOrderToUnlinkedObs(any(Order.class), any(Encounter.class));
    }

    @Test
    public void shouldCreateNewSurgeryOrderWhenDifferentSurgicalAppointmentOnSameEncounter() {
        // Surgery A already has a Surgery Order. Surgery B must get its own new Surgery Order.
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        // Surgery B appointment has no order yet
        SurgicalAppointment apptB = new SurgicalAppointment();

        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-B")).thenReturn(apptB);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-B")).thenReturn(apptB);

        when(orderService.getOrderTypeByName("Surgery Order")).thenReturn(orderTypeWithName("Surgery Order"));
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        Encounter encounter = encounterWithProvider();
        // Surgery A order already on encounter
        encounter.setOrders(Collections.singleton(existingOrderOfType("Surgery Order")));
        // New form is for Surgery B
        encounter.addObs(selectSurgeryObs("appt-uuid-B"));

        command.save(bet, encounter, new EncounterTransaction());

        // Must create a new Surgery Order for Surgery B, not reuse Surgery A's order
        verify(orderService).saveOrder(any(Order.class), eq(null));
        verify(surgicalAppointmentService).save(apptB);
    }

    @Test
    public void shouldReuseExistingGeneralOrderAndLinkUnlinkedObs() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        Encounter encounter = encounterWithProvider();
        encounter.setOrders(Collections.singleton(existingOrderOfType("General Order")));

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService, never()).saveOrder(any(Order.class), any());
        verify(surgeryObsOrderLinkDao).assignOrderToUnlinkedObs(any(Order.class), any(Encounter.class));
    }

    @Test
    public void shouldCreateSurgeryOrderEvenWhenGeneralOrderExistsOnEncounter() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        Encounter encounter = encounterWithProvider();
        encounter.addObs(selectSurgeryObs("appt-uuid-999"));
        encounter.setOrders(Collections.singleton(existingOrderOfType("General Order")));

        OrderType surgeryType = orderTypeWithName("Surgery Order");
        when(orderService.getOrderTypeByName("Surgery Order")).thenReturn(surgeryType);
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        SurgicalAppointment appt = new SurgicalAppointment();
        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-999")).thenReturn(appt);

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService).saveOrder(any(Order.class), eq(null));
    }

    @Test
    public void shouldReturnGracefullyWhenOrderTypeNotConfigured() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        when(orderService.getOrderTypeByName(any(String.class))).thenReturn(null);

        command.save(bet, encounterWithProvider(), new EncounterTransaction());

        verify(orderService, never()).saveOrder(any(Order.class), any());
        verify(surgeryObsOrderLinkDao, never()).assignOrderToUnlinkedObs(any(Order.class), any(Encounter.class));
    }

    @Test
    public void shouldDetectSelectSurgeryObsOnEncounter() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        when(orderService.getOrderTypeByName("Surgery Order")).thenReturn(orderTypeWithName("Surgery Order"));
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName("OUTPATIENT")).thenReturn(new CareSetting());
        

        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("nested-appt-uuid"))
                .thenReturn(new SurgicalAppointment());

        Encounter encounter = encounterWithProvider();
        encounter.addObs(selectSurgeryObs("nested-appt-uuid"));

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService).getOrderTypeByName("Surgery Order");
        // Called twice: once to check for existing order, once to link the appointment to the new order
        verify(surgicalAppointmentService, org.mockito.Mockito.times(2)).getSurgicalAppointmentByUuid("nested-appt-uuid");
    }

    @Test
    public void shouldNotCreateOrderWhenNoProviderOnEncounter() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        when(orderService.getOrderTypeByName(anyString())).thenReturn(orderTypeWithName("General Order"));
        
        when(orderService.getCareSettingByName(anyString())).thenReturn(new CareSetting());

        Encounter encounter = new Encounter();
        encounter.setOrders(new HashSet<>());
        encounter.setEncounterProviders(Collections.emptySet());

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService, never()).saveOrder(any(Order.class), any());
        verify(surgeryObsOrderLinkDao, never()).assignOrderToUnlinkedObs(any(Order.class), any(Encounter.class));
    }

    @Test
    public void shouldNotSaveSurgicalAppointmentWhenNotFoundByUuid() {
        BahmniEncounterTransaction bet = new BahmniEncounterTransaction();
        bet.setObservations(new ArrayList<>());

        OrderType surgeryType = orderTypeWithName("Surgery Order");
        when(orderService.getOrderTypeByName("Surgery Order")).thenReturn(surgeryType);
        when(orderService.saveOrder(any(Order.class), eq(null))).thenReturn(new Order());
        when(orderService.getCareSettingByName(anyString())).thenReturn(new CareSetting());
        

        PowerMockito.when(Context.getService(SurgicalAppointmentService.class)).thenReturn(surgicalAppointmentService);
        when(surgicalAppointmentService.getSurgicalAppointmentByUuid("appt-uuid-123")).thenReturn(null);

        Encounter encounter = encounterWithProvider();
        encounter.addObs(selectSurgeryObs("appt-uuid-123"));

        command.save(bet, encounter, new EncounterTransaction());

        verify(orderService).saveOrder(any(Order.class), eq(null));
        verify(surgicalAppointmentService, never()).save(any());
    }

    // --- helpers ---

    private Obs selectSurgeryObs(String apptUuid) {
        Obs obs = new Obs();
        obs.setConcept(selectSurgeryConcept);
        obs.setValueComplex(apptUuid);
        obs.setVoided(false);
        return obs;
    }

    private OrderType orderTypeWithName(String name) {
        OrderType ot = new OrderType();
        ot.setName(name);
        return ot;
    }

    private Order existingOrderOfType(String typeName) {
        Order order = new Order();
        order.setOrderType(orderTypeWithName(typeName));
        order.setVoided(false);
        return order;
    }

    private Encounter encounterWithProvider() {
        Encounter encounter = new Encounter();
        Provider provider = new Provider();
        EncounterProvider ep = new EncounterProvider();
        ep.setProvider(provider);
        encounter.setEncounterProviders(Collections.singleton(ep));
        encounter.setOrders(new HashSet<>());
        return encounter;
    }
}
