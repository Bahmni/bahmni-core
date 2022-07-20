package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.OrderFrequency;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.TestUsernameAuthenticationScheme;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.builder.ConceptBuilder;
import org.openmrs.module.bahmniemrapi.drugogram.contract.RegimenRow;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({LocaleUtility.class})
public class DrugOrderToRegimenMapperTest {

    private DrugOrderToRegimenMapper drugOrderToRegimenMapper;


    Concept bdq;

    Concept dlm;


    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(LocaleUtility.class);
        PowerMockito.when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        Context.setUserContext(new UserContext(new TestUsernameAuthenticationScheme()));
        bdq = new ConceptBuilder().withName("Bedaquiline").withDataType("N/A").build();
        dlm = new ConceptBuilder().withName("Delamanid").withDataType("N/A").build();
        drugOrderToRegimenMapper = new DrugOrderToRegimenMapper();
    }


    @Test
    public void shouldSetErrorWhenDrugIsStartedAndStoppedOnTheSameDate() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2016-01-10"));
        drugOrder.setAutoExpireDate(sdf.parse("2016-01-10"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2016-01-10"));
        assertEquals(value, "Error");
    }

    @Test
    public void shouldSetDoseWhenDrugIsWithinTheRange() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2016-01-10"));
        drugOrder.setAutoExpireDate(sdf.parse("2016-01-15"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String dose = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2016-01-12"));
        assertEquals(dose, "200.0");
    }

    @Test
    public void shouldSetStopWhenDrugIsStoppedOnRowDate() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2016-01-10"));
        drugOrder.setAutoExpireDate(sdf.parse("2016-01-20"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2016-01-20"));
        assertEquals(value, "Stop");
    }

    @Test
    public void shouldSetEmptyStringWhenDrugOrderIsStoppedBeforeRowDate() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2015-01-10"));
        drugOrder.setAutoExpireDate(sdf.parse("2015-01-20"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2015-01-21"));
        assertEquals(value, "");
    }

    @Test
    public void shouldSetEmptyStringWhenDrugOrderIsStoppedAfterRowDate() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2015-01-10"));
        drugOrder.setAutoExpireDate(sdf.parse("2015-01-20"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2014-01-21"));
        assertEquals(value, "");
    }

    @Test
    public void shouldSetDoseForActiveDrugRegimen() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2015-01-10"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2015-01-21"));
        assertEquals(value, "200.0");
    }

    @Test
    public void shouldSetEmptyForActiveDrugRegimenAfterTheStartDate() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        drugOrder.setDateActivated(sdf.parse("2016-01-10"));
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);

        String value = drugOrderToRegimenMapper.getValueForField(drugOrder, sdf.parse("2015-01-21"));
        assertEquals(value, "");
    }


    @Test
    public void shouldCreateTwoRegimenRowsForSingleDrugOrder() throws ParseException {
        DrugOrder drugOrder = new DrugOrder();
        OrderFrequency orderFrequency = new OrderFrequency();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date dateActivated = sdf.parse("2015-01-10");
        Date dateStopped = sdf.parse("2015-01-20");
        drugOrder.setDateActivated(dateActivated);
        drugOrder.setAutoExpireDate(dateStopped);
        drugOrder.setDose(200.0);
        drugOrder.setFrequency(orderFrequency);
        drugOrder.setConcept(bdq);

        Set<Concept> headerConfig = new LinkedHashSet<>();
        headerConfig.add(bdq);


        TreatmentRegimen treatmentRegimen = drugOrderToRegimenMapper.map(asList((Order) drugOrder), headerConfig);

        List<RegimenRow> regimenRows = new ArrayList<>();
        regimenRows.addAll(treatmentRegimen.getRows());

        assertEquals(2, regimenRows.size());
        assertEquals(regimenRows.get(0).getDate(), dateActivated);
        assertEquals(regimenRows.get(1).getDate(), dateStopped);

        assertEquals(1, regimenRows.get(0).getDrugs().size());
        assertEquals(1, regimenRows.get(1).getDrugs().size());
        assertEquals("200.0", regimenRows.get(0).getDrugs().get("Bedaquiline"));
        assertEquals("Stop", regimenRows.get(1).getDrugs().get("Bedaquiline"));
    }

    @Test
    public void shouldCreateRegimenRowsForDrugOrdersOfSameType() throws ParseException {
        Set<Concept> headerConfig = new LinkedHashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        headerConfig.add(bdq);


       List<Order> drugOrders =  constructDrugOrdersForList(Arrays.asList(
                new DrugOrderData("2016-01-10", "2016-01-20", bdq, 200.0),
                new DrugOrderData("2016-01-21", "2016-01-30", bdq, 300.0),
                new DrugOrderData("2016-01-30", "2016-02-20", bdq, 200.0),
                new DrugOrderData("2016-03-03", "2016-03-03", bdq, 1000.0)
                ));

        TreatmentRegimen treatmentRegimen = drugOrderToRegimenMapper.map(drugOrders, headerConfig);

        List<RegimenRow> regimenRows = new ArrayList<>();
        regimenRows.addAll(treatmentRegimen.getRows());

        assertEquals(6, regimenRows.size());
        assertEquals(regimenRows.get(0).getDate(),sdf.parse("2016-01-10"));
        assertEquals(regimenRows.get(1).getDate(),sdf.parse("2016-01-20"));
        assertEquals(regimenRows.get(2).getDate(),sdf.parse("2016-01-21"));
        assertEquals(regimenRows.get(3).getDate(),sdf.parse("2016-01-30"));
        assertEquals(regimenRows.get(4).getDate(),sdf.parse("2016-02-20"));
        assertEquals(regimenRows.get(5).getDate(),sdf.parse("2016-03-03"));

        assertEquals(1, regimenRows.get(0).getDrugs().size());
        assertEquals("200.0", regimenRows.get(0).getDrugs().get("Bedaquiline"));
        assertEquals("Stop", regimenRows.get(1).getDrugs().get("Bedaquiline"));
        assertEquals("300.0", regimenRows.get(2).getDrugs().get("Bedaquiline"));
        assertEquals("200.0", regimenRows.get(3).getDrugs().get("Bedaquiline"));
        assertEquals("Stop", regimenRows.get(4).getDrugs().get("Bedaquiline"));
        assertEquals("Error", regimenRows.get(5).getDrugs().get("Bedaquiline"));
    }

    @Test
    public void shouldCreateRegimenRowsForTwoDifferentDrugOrders() throws ParseException {
        Set<Concept> headerConfig = new LinkedHashSet<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        List<Order> drugOrders =  constructDrugOrdersForList(Arrays.asList(
                new DrugOrderData("2016-01-10", "2016-01-20", bdq, 200.0),
                new DrugOrderData("2016-01-21", "2016-01-30", bdq, 300.0),
                new DrugOrderData("2016-01-30", "2016-02-20", bdq, 400.0),
                new DrugOrderData("2016-03-03", "2016-03-03", bdq, 450.0),
                new DrugOrderData("2016-01-01", "2016-01-10", dlm, 500.0),
                new DrugOrderData("2016-01-11", "2016-01-20", dlm, 600.0),
                new DrugOrderData("2016-01-10", "2016-03-03", dlm, 550.0)
        ));

        TreatmentRegimen treatmentRegimen = drugOrderToRegimenMapper.map(drugOrders, headerConfig);

        List<RegimenRow> regimenRows = new ArrayList<>();
        regimenRows.addAll(treatmentRegimen.getRows());

        assertEquals(8, regimenRows.size());
        assertEquals(regimenRows.get(0).getDate(),sdf.parse("2016-01-01"));
        assertEquals(regimenRows.get(1).getDate(),sdf.parse("2016-01-10"));
        assertEquals(regimenRows.get(2).getDate(),sdf.parse("2016-01-11"));
        assertEquals(regimenRows.get(3).getDate(),sdf.parse("2016-01-20"));
        assertEquals(regimenRows.get(4).getDate(),sdf.parse("2016-01-21"));
        assertEquals(regimenRows.get(5).getDate(),sdf.parse("2016-01-30"));
        assertEquals(regimenRows.get(6).getDate(),sdf.parse("2016-02-20"));
        assertEquals(regimenRows.get(7).getDate(),sdf.parse("2016-03-03"));

        assertEquals(1, regimenRows.get(0).getDrugs().size());
        assertEquals(2, regimenRows.get(1).getDrugs().size());
        assertEquals(2, regimenRows.get(2).getDrugs().size());
        assertEquals(2, regimenRows.get(3).getDrugs().size());
        assertEquals(2, regimenRows.get(4).getDrugs().size());
        assertEquals(2, regimenRows.get(5).getDrugs().size());
        assertEquals(2, regimenRows.get(6).getDrugs().size());
        assertEquals(2, regimenRows.get(7).getDrugs().size());


        assertNull(regimenRows.get(0).getDrugs().get("Bedaquiline"));
        assertEquals("200.0", regimenRows.get(1).getDrugs().get("Bedaquiline"));
        assertEquals("200.0", regimenRows.get(2).getDrugs().get("Bedaquiline"));
        assertEquals("Stop", regimenRows.get(3).getDrugs().get("Bedaquiline"));
        assertEquals("300.0", regimenRows.get(4).getDrugs().get("Bedaquiline"));
        assertEquals("400.0", regimenRows.get(5).getDrugs().get("Bedaquiline"));
        assertEquals("Stop", regimenRows.get(6).getDrugs().get("Bedaquiline"));
        assertEquals("Error", regimenRows.get(7).getDrugs().get("Bedaquiline"));

        assertEquals("500.0",regimenRows.get(0).getDrugs().get("Delamanid"));
        assertEquals("550.0", regimenRows.get(1).getDrugs().get("Delamanid"));
        assertEquals("Error", regimenRows.get(2).getDrugs().get("Delamanid"));
        assertEquals("550.0", regimenRows.get(3).getDrugs().get("Delamanid"));
        assertEquals("550.0", regimenRows.get(4).getDrugs().get("Delamanid"));
        assertEquals("550.0", regimenRows.get(5).getDrugs().get("Delamanid"));
        assertEquals("550.0", regimenRows.get(6).getDrugs().get("Delamanid"));
        assertEquals("Stop", regimenRows.get(7).getDrugs().get("Delamanid"));
    }




    private List<Order> constructDrugOrdersForList(List<DrugOrderData> drugOrderList) throws ParseException {
        List<Order> drugOrders = new ArrayList<>();

        for (DrugOrderData orderData : drugOrderList) {
            DrugOrder drugOrder = new DrugOrder();
            OrderFrequency orderFrequency = new OrderFrequency();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            Date dateActivated = sdf.parse(orderData.getDateActivated());
            Date dateStopped = sdf.parse(orderData.getDateStopped());
            drugOrder.setDateActivated(dateActivated);
            drugOrder.setAutoExpireDate(dateStopped);
            drugOrder.setDose(orderData.getDose());
            drugOrder.setFrequency(orderFrequency);
            drugOrder.setConcept(orderData.getConcept());
            drugOrders.add((Order)drugOrder);
        }

        return drugOrders;
    }

    private class DrugOrderData {
        private String dateActivated;
        private String dateStopped;
        private Double dose;
        private Concept concept;

        private DrugOrderData(String dateActivated, String dateStopped, Concept concept, Double dose) {
            this.dateActivated = dateActivated;
            this.dose = dose;
            this.dateStopped = dateStopped;
            this.concept = concept;
        }


        public String getDateActivated() {
            return dateActivated;
        }

        public String getDateStopped() {
            return dateStopped;
        }

        public Double getDose() {
            return dose;
        }

        public void setDose(Double dose) {
            this.dose = dose;
        }

        public Concept getConcept() {
            return concept;
        }
    }
}
