package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.service.BahmniOrderService;
import org.bahmni.module.bahmnicore.service.OrderService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.CareSetting;
import org.openmrs.Concept;
import org.openmrs.Order;
import org.openmrs.OrderType;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.Provider;
import org.openmrs.module.bahmniemrapi.order.contract.BahmniOrder;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(LocaleUtility.class)
public class BahmniOrderServiceImplTest {

    private BahmniOrderService bahmniOrderService;

    private String personUUID = "12345";
    private String visitUUID = "54321";
    private Order order;
    private Concept concept;
    private Provider provider;
    private Patient patient;

    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private OrderService orderService;
    @Mock
    private ConceptMapper conceptMapper;


    @Before
    public void setUp() {
        initMocks(this);

        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        concept= new org.bahmni.test.builder.ConceptBuilder().withUUID("otUUID").build();
        EncounterTransaction.Concept encounterTransactionconcept = new EncounterTransaction.Concept();
        encounterTransactionconcept.setName("Concept for order");
        encounterTransactionconcept.setConceptClass("otClass");
        encounterTransactionconcept.setDataType("N/A");
        encounterTransactionconcept.setUuid("otUUID");
        when(conceptMapper.map(any(Concept.class))).thenReturn(encounterTransactionconcept);
        bahmniOrderService = new BahmniOrderServiceImpl(orderService, bahmniObsService, conceptMapper);

    }

    @Test
    public void shouldGetBahmniOrdersForOrderType() throws Exception {
        when(orderService.getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), 2, null, "someOrderTypeUuid", true, null);
        verify(orderService).getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2);
        Assert.assertEquals(3, bahmniOrders.size());
    }

    @Test
    public void shouldGetAllOrdersIfNumberOfVisitsIsNullOrZero() throws Exception {
        when(orderService.getAllOrders(personUUID, "someOrderTypeUuid", null, null, null)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), null, null, "someOrderTypeUuid", true, null);
        verify(orderService).getAllOrders(personUUID, "someOrderTypeUuid", null, null, null);
        Assert.assertEquals(3, bahmniOrders.size());
    }

    @Test
    public void shouldNotSetObservationIfIncludeObsFlagIsSetToFalse() throws Exception {
        when(orderService.getAllOrders(personUUID, "someOrderTypeUuid", null, null, null)).thenReturn(Arrays.asList(createOrder(), createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Arrays.asList(concept), null, null, "someOrderTypeUuid", false, null);
        verify(orderService).getAllOrders(personUUID, "someOrderTypeUuid", null, null, null);
        Assert.assertEquals(3, bahmniOrders.size());
        Assert.assertNull(bahmniOrders.get(0).getBahmniObservations());
    }

    @Test
    public void shouldGetBahmniOrdersForOrder() throws Exception {
        Order order = createOrder();
        when(orderService.getOrderByUuid("someOrderUuid")).thenReturn(order);
        bahmniOrderService.ordersForOrderUuid(personUUID, Arrays.asList(concept), null, "someOrderUuid");
        verify(bahmniObsService).observationsFor(personUUID, Arrays.asList(concept), null, null, false, order, null, null);
    }

    @Test
    public void shouldGetBahmniOrdersForVisit() throws Exception {
        when(orderService.getAllOrdersForVisitUuid(visitUUID, "someOrderTypeUuid")).thenReturn(Arrays.asList(createOrder(), createOrder()));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForVisit(visitUUID, "someOrderTypeUuid", null, Arrays.asList(concept));
        verify(bahmniObsService).getObservationForVisit(visitUUID, null, Arrays.asList(concept), false, order);
        verify(orderService).getAllOrdersForVisitUuid(visitUUID, "someOrderTypeUuid");
        Assert.assertEquals(2, bahmniOrders.size());
    }


    @Test
    public void shouldGetChildOrder() throws Exception {
        Order order = createOrder();
        bahmniOrderService.getChildOrder(order);
        verify(orderService,times(1)).getChildOrder(order);
    }

    @Test
    public void shouldGetAppropriateBahmniOrdersDetails() {
        Order order = createOrder();
        when(orderService.getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2)).thenReturn(Collections.singletonList(order));
        List<BahmniOrder> bahmniOrders = bahmniOrderService.ordersForOrderType(personUUID, Collections.singletonList(concept), 2, null, "someOrderTypeUuid", true, null);
        BahmniOrder bahmniOrder = bahmniOrders.get(0);
        verify(orderService).getAllOrdersForVisits(personUUID, "someOrderTypeUuid", 2);
        Assert.assertEquals("otUUID", bahmniOrder.getConcept().getUuid());
        Assert.assertEquals("someOrderTypeUuid", bahmniOrder.getOrderTypeUuid());
        Assert.assertEquals("88887777-eeee-4326-bb05-c6e11fe31234", bahmniOrder.getProviderUuid());
        Assert.assertEquals("Superman", bahmniOrder.getProvider());
        Assert.assertEquals(Order.FulfillerStatus.COMPLETED, bahmniOrder.getFulfillerStatus());
    }

    private Order createOrder() {
        order = new Order();
        patient = new Patient();
        patient.setId(1);
        patient.setUuid(personUUID);
        Person person = new Person();
        person.setNames(Collections.singleton(new PersonName("Superman","", "")));
        OrderType orderType = new OrderType();
        provider = new Provider();
        orderType.setId(1);
        orderType.setUuid("someOrderTypeUuid");
        order.setOrderType(orderType);
        provider.setId(2);
        provider.setUuid("88887777-eeee-4326-bb05-c6e11fe31234");
        provider.setPerson(person);
        order.setOrderer(provider);
        order.setConcept(concept);
        order.setId(1);
        order.setPatient(patient);
        CareSetting careSetting = new CareSetting();
        careSetting.setId(1);
        order.setCareSetting(careSetting);
        order.setFulfillerStatus(Order.FulfillerStatus.COMPLETED);
        return order;
    }
}
