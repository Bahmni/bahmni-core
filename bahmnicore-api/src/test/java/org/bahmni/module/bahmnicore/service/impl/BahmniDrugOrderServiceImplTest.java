package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.api.PatientService;
import org.openmrs.module.bahmniemrapi.drugorder.contract.BahmniDrugOrder;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anySet;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniDrugOrderServiceImplTest {

    public static final String PATIENT_PROGRAM_UUID = "patient-program-uuid";
    public static final String PATIENT_UUID = "patient-uuid";

    @Mock
    BahmniProgramWorkflowService bahmniProgramWorkflowService;
    @Mock
    PatientService patientService;
    @Mock
    OrderService orderService;
    @Mock
    OrderDao orderDao;

    @InjectMocks
    BahmniDrugOrderServiceImpl bahmniDrugOrderService;
    private final CareSetting mockCareSetting = mock(CareSetting.class);
    private final Patient mockPatient = mock(Patient.class);
    private final OrderType mockOrderType = mock(OrderType.class);
    private HashSet<Concept> conceptsToFilter;
    private final ArgumentCaptor<Date> dateArgumentCaptor = ArgumentCaptor.forClass(Date.class);
    private final List<Encounter> encounters = new ArrayList<>();


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        encounters.add(new Encounter());

        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(PATIENT_PROGRAM_UUID)).thenReturn(encounters);
        when(patientService.getPatientByUuid(PATIENT_UUID)).thenReturn(mockPatient);
        when(orderService.getCareSettingByName(anyString())).thenReturn(mockCareSetting);
        when(orderService.getOrderTypeByName("Drug order")).thenReturn(mockOrderType);
        when(orderService.getOrderTypeByUuid(OrderType.DRUG_ORDER_TYPE_UUID)).thenReturn(mockOrderType);

        final Concept concept = mock(Concept.class);
        conceptsToFilter = new HashSet<Concept>() {{
            add(concept);
        }};

    }

    @Test
    public void shouldGetActiveDrugOrdersOfAPatientProgram() throws ParseException {
        when(orderDao.getActiveOrders(any(Patient.class), any(OrderType.class), any(CareSetting.class),
                dateArgumentCaptor.capture(), anySet(), eq(null), eq(null), eq(null), anyCollection())).thenReturn(new ArrayList<Order>());

       bahmniDrugOrderService.getDrugOrders(PATIENT_UUID, true, conceptsToFilter, null, PATIENT_PROGRAM_UUID);

        final Date value = dateArgumentCaptor.getValue();
        verify(orderDao, times(2)).getActiveOrders(mockPatient, mockOrderType, mockCareSetting, value, conceptsToFilter, null, null, null, encounters);
    }

    @Test
    public void shouldReturnEmptyListWhenNoEncountersAssociatedWithPatientProgram() throws ParseException {
        when(bahmniProgramWorkflowService.getEncountersByPatientProgramUuid(PATIENT_PROGRAM_UUID)).thenReturn(new HashSet<Encounter>());

        final List<BahmniDrugOrder> drugOrders = bahmniDrugOrderService.getDrugOrders(PATIENT_UUID, true, null, null, PATIENT_PROGRAM_UUID);

        verifyNoMoreInteractions(orderDao);
        assertTrue(drugOrders.isEmpty());
    }

    @Test
    public void shouldGetAllDrugOrdersOfAPatientProgram() throws ParseException {
        bahmniDrugOrderService.getDrugOrders(PATIENT_UUID, null, conceptsToFilter, null, PATIENT_PROGRAM_UUID);

        verify(orderDao).getAllOrders(mockPatient, mockOrderType, conceptsToFilter, null, encounters);
    }

    @Test
    public void shouldNotConsiderEncountersToFetchDrugOrdersIfPatientProgramUuidIsNull() throws Exception {
        bahmniDrugOrderService.getDrugOrders(PATIENT_UUID, null, conceptsToFilter, null, null);
        List<Encounter> encounters = null ;

        verify(orderDao).getAllOrders(mockPatient, mockOrderType,conceptsToFilter, null, encounters);
        verifyNoMoreInteractions(bahmniProgramWorkflowService);
    }
}
