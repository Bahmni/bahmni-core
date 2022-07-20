package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.TestUsernameAuthenticationScheme;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class})
public class DrugOrderToTreatmentRegimenMapperTest {

    private DrugOrderToRegimenMapper drugOrderToTreatmentRegimenMapper;

    public static final String DAY_DURATION_UNIT = "Day";


    @Before
    public void setUp() throws Exception {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        Context.setUserContext(new UserContext(new TestUsernameAuthenticationScheme()));
        drugOrderToTreatmentRegimenMapper = new DrugOrderToRegimenMapper();
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnSameDateAndEndOnDifferentDateAndCrossEachOther() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 6)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 6)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("Stop", thirdRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnSameDate() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(2, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnDifferentDateDoesntOverlap() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 3)).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", thirdRow.getDrugs().get("Paracetemol"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibeprofen"));
        assertEquals("Stop", fourthRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartAndEndOnDifferentDateAndOverlaps() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 2)).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), stoppedDateRow.getDate());
        assertEquals("1000.0", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", thirdRow.getDrugs().get("Paracetemol"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("Ibeprofen"));
        assertEquals("Stop", fourthRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldSetErrorWhenDrugIsStartedAndStoppedOnTheSameDay() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(1, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(1, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("Error", startDateRow.getDrugs().get("Ibeprofen"));
    }


    @Test
    public void shouldMapTo2RowsIf2DrugsAreStartedAndStoppedOnTheSameDayButOnDifferentDays() throws Exception {
        // I know the test name sounds weird. If you have any better name, feel free to change it.
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(now).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetamol = new DrugOrderBuilder().withDrugName("Paracetamol").withDateActivated(addDays(now, 2)).withDose(500.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Paracetamol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetamol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetamol", headerIterator.next().getName());
        assertEquals(2, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("Error", firstRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, firstRow.getDrugs().get("Paracetamol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now,2)), secondRow.getDate());
        assertEquals(null, secondRow.getDrugs().get("Ibeprofen"));
        assertEquals("Error", secondRow.getDrugs().get("Paracetamol"));

    }

    @Test
    public void shouldNotFetchTheDrugIfTheDrugIsStoppedBeforeScheduledDate() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withScheduledDate(addDays(now, 10)).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();

        drugOrders.add(ibeprofen);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(1, treatmentRegimen.getHeaders().size());
        assertEquals(2, treatmentRegimen.getRows().size());
    }



    @Test
    public void shouldMapScheduledDrugOrders() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(now).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withScheduledDate(now).withDose(500.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 3)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder lajvanti = new DrugOrderBuilder().withDrugName("Lajvanti").withScheduledDate(addDays(now, 2)).withDose(3.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Lajvanti").withUUID("Lajvanti uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(caffeine);
        drugOrders.add(lajvanti);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(3, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals("Lajvanti", headerIterator.next().getName());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("Error", firstRow.getDrugs().get("P 500mg"));
        assertEquals("500.0", firstRow.getDrugs().get("Caffeine"));
        assertEquals(null, firstRow.getDrugs().get("Lajvanti"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals(null, secondRow.getDrugs().get("P 500mg"));
        assertEquals("500.0", secondRow.getDrugs().get("Caffeine"));
        assertEquals("3.0", secondRow.getDrugs().get("Lajvanti"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("P 500mg"));
        assertEquals("Stop", thirdRow.getDrugs().get("Caffeine"));
        assertEquals("3.0", thirdRow.getDrugs().get("Lajvanti"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("P 500mg"));
        assertEquals(null, fourthRow.getDrugs().get("Caffeine"));
        assertEquals("Stop", fourthRow.getDrugs().get("Lajvanti"));
    }

    @Test
    public void shouldRetrieveIfTheDrugStartedAndStoppedOnTheSameDayLiesBetweenOtherDrug() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder revisedPmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(addDays(now, 2)).withDose(10.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withDateActivated(addDays(now, 2)).withDose(600.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(revisedPmg);
        drugOrders.add(caffeine);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("P 500mg"));
        assertEquals(null, firstRow.getDrugs().get("Caffeine"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("10.0", secondRow.getDrugs().get("P 500mg"));
        assertEquals("Error", secondRow.getDrugs().get("Caffeine"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("P 500mg"));
        assertEquals(null, thirdRow.getDrugs().get("Caffeine"));
    }

    @Test
    public void shouldRetrieveIfTheDrugStartsOntheDayOfTheOtherDrugsStopped() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder revisedPmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(addDays(now, 2)).withDose(10.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withDateActivated(addDays(now, 10)).withDose(600.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 12)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(revisedPmg);
        drugOrders.add(caffeine);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals(4, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("1000.0", firstRow.getDrugs().get("P 500mg"));
        assertEquals(null, firstRow.getDrugs().get("Caffeine"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("10.0", secondRow.getDrugs().get("P 500mg"));
        assertEquals(null, secondRow.getDrugs().get("Caffeine"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("P 500mg"));
        assertEquals("600.0", thirdRow.getDrugs().get("Caffeine"));

        RegimenRow fourthRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 12)), fourthRow.getDate());
        assertEquals(null, fourthRow.getDrugs().get("P 500mg"));
        assertEquals("Stop", fourthRow.getDrugs().get("Caffeine"));
    }

    @Test
    public void shouldRetrieveIfTheDrugStartsOntheDayOfTheOtherDrugsStoppedAndTheDrugIsRevised() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder pmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(now).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder revisedPmg = new DrugOrderBuilder().withDrugName("P 500mg").withDateActivated(now).withDose(500.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("P 500mg").withUUID("P 500mg uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder caffeine = new DrugOrderBuilder().withDrugName("Caffeine").withDateActivated(addDays(now, 10)).withDose(600.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 10)).withOrderAction(Order.Action.NEW).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        DrugOrder revisedCaffeine = new DrugOrderBuilder().withDrugName("Caffeine").withDateActivated(addDays(now, 10)).withDose(800.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 12)).withOrderAction(Order.Action.REVISE).withConcept(new ConceptBuilder().withName("Caffeine").withUUID("Caffeine uuid").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(pmg);
        drugOrders.add(revisedPmg);
        drugOrders.add(caffeine);
        drugOrders.add(revisedCaffeine);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("P 500mg", headerIterator.next().getName());
        assertEquals("Caffeine", headerIterator.next().getName());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow firstRow = rowIterator.next();
        assertEquals(getOnlyDate(now), firstRow.getDate());
        assertEquals("Error", firstRow.getDrugs().get("P 500mg"));
        assertEquals(null, firstRow.getDrugs().get("Caffeine"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 10)), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("P 500mg"));
        assertEquals("Error", secondRow.getDrugs().get("Caffeine"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 12)), thirdRow.getDate());
        assertEquals(null, thirdRow.getDrugs().get("P 500mg"));
        assertEquals("Stop", thirdRow.getDrugs().get("Caffeine"));
    }

    private Date addDays(Date now, int days) {
        Calendar c = Calendar.getInstance();
        c.setTime(now);
        c.add(Calendar.DATE, days);
        return c.getTime();
    }

    public Date getOnlyDate(Date date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        return sdf.parse(sdf.format(date));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnSameDateAndOneEndsInFiveDaysAnotherContinues() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(2, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnDifferentDatesAndOneEndsInFiveDaysAnotherContinues() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 2)).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("1000.0", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", secondRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));
    }


    @Test
    public void shouldMapDrugOrderWhichHaveNoStopDate() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(now).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(1, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(1, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("200.0", startDateRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldMapDrugOrdersWhichStartOnDifferentDatesAndOneStoppedBeforeAnotherStartsContinues() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 5)).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, null);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals(3, treatmentRegimen.getRows().size());
        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, secondRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals(null, stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));
    }


    @Test
    public void shouldFetchTheAsTheOrderSpecifiedInTheConceptNames() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();
        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDose(1000.0).withFrequency(DAY_DURATION_UNIT).withAutoExpireDate(addDays(now, 2)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(addDays(now, 5)).withDose(200.0).withFrequency(DAY_DURATION_UNIT).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        ConceptName paracetamolConceptName = new ConceptName("Paracetemol", new Locale("en", "in"));
        ConceptName ibeprofenConceptName = new ConceptName("Ibeprofen", new Locale("en", "in"));

        Concept paracetemolConcept= new ConceptBuilder().withName(paracetamolConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();
        Concept ibeprofenConcept= new ConceptBuilder().withName(ibeprofenConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();

        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetemolConcept);
        concepts.add(ibeprofenConcept);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(false, headerIterator.hasNext());
        assertEquals(3, treatmentRegimen.getRows().size());

        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1000.0", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 2)), secondRow.getDate());
        assertEquals("Stop", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, secondRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals(null, stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("200.0", stoppedDateRow.getDrugs().get("Paracetemol"));
    }

    @Test
    public void shouldFetchTheAsTheOrderSpecifiedInTheConceptNamesWithVariableDosing() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();

        String dosingInstructions = "{\"morningDose\": \"1\", \"afternoonDose\": \"2\", \"eveningDose\": \"3\"}";

        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDosingInstructions(dosingInstructions).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDosingInstructions(dosingInstructions).withDateActivated(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        ConceptName paracetamolConceptName = new ConceptName("Paracetemol", new Locale("en", "in"));
        ConceptName ibeprofenConceptName = new ConceptName("Ibeprofen", new Locale("en", "in"));

        Concept paracetemolConcept= new ConceptBuilder().withName(paracetamolConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();
        Concept ibeprofenConcept= new ConceptBuilder().withName(ibeprofenConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Ibeprofen").build();

        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetemolConcept);
        concepts.add(ibeprofenConcept);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(false, headerIterator.hasNext());
        assertEquals(3, treatmentRegimen.getRows().size());

        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1-2-3", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), secondRow.getDate());
        assertEquals("1-2-3", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals("1-2-3", secondRow.getDrugs().get("Paracetemol"));

        RegimenRow stoppedDateRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), stoppedDateRow.getDate());
        assertEquals("Stop", stoppedDateRow.getDrugs().get("Ibeprofen"));
        assertEquals("1-2-3", stoppedDateRow.getDrugs().get("Paracetemol"));
    }


    @Test
    public void shouldFetchTheAsTheOrderSpecifiedInTheConceptNamesWithVariableDosingAndOneStoppedOnTheSameDay() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        Date now = new Date();

        String dosingInstructions = "{\"morningDose\": \"1\", \"afternoonDose\": \"2\", \"eveningDose\": \"3\"}";

        DrugOrder ibeprofen = new DrugOrderBuilder().withDrugName("Ibeprofen").withDateActivated(now).withDosingInstructions(dosingInstructions).withAutoExpireDate(addDays(now, 5)).withConcept(new ConceptBuilder().withName("Ibeprofen").withSet(false).withDataType("N/A").build()).build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDosingInstructions(dosingInstructions).withDateActivated(addDays(now, 3)).withAutoExpireDate(addDays(now, 3)).withConcept(new ConceptBuilder().withName("Paracetemol").withSet(false).withDataType("N/A").build()).build();
        drugOrders.add(ibeprofen);
        drugOrders.add(paracetemol);

        ConceptName paracetamolConceptName = new ConceptName("Paracetemol", new Locale("en", "in"));
        ConceptName ibeprofenConceptName = new ConceptName("Ibeprofen", new Locale("en", "in"));

        Concept paracetemolConcept= new ConceptBuilder().withName(paracetamolConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();
        Concept ibeprofenConcept= new ConceptBuilder().withName(ibeprofenConceptName).withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Ibeprofen").build();

        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetemolConcept);
        concepts.add(ibeprofenConcept);

        TreatmentRegimen treatmentRegimen = drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts);

        assertNotNull(treatmentRegimen);
        assertEquals(2, treatmentRegimen.getHeaders().size());
        Iterator<EncounterTransaction.Concept> headerIterator = treatmentRegimen.getHeaders().iterator();
        assertEquals("Paracetemol", headerIterator.next().getName());
        assertEquals("Ibeprofen", headerIterator.next().getName());
        assertEquals(false, headerIterator.hasNext());
        assertEquals(3, treatmentRegimen.getRows().size());

        Iterator<RegimenRow> rowIterator = treatmentRegimen.getRows().iterator();

        RegimenRow startDateRow = rowIterator.next();
        assertEquals(getOnlyDate(now), startDateRow.getDate());
        assertEquals("1-2-3", startDateRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, startDateRow.getDrugs().get("Paracetemol"));

        RegimenRow secondRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 3)), secondRow.getDate());
        assertEquals("1-2-3", secondRow.getDrugs().get("Ibeprofen"));
        assertEquals("Error", secondRow.getDrugs().get("Paracetemol"));

        RegimenRow thirdRow = rowIterator.next();
        assertEquals(getOnlyDate(addDays(now, 5)), thirdRow.getDate());
        assertEquals("Stop", thirdRow.getDrugs().get("Ibeprofen"));
        assertEquals(null, thirdRow.getDrugs().get("Paracetemol"));
    }
}
