package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToRegimenMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
public class DrugOGramControllerTest {
    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    private DrugOrderToRegimenMapper drugOrderToTreatmentRegimenMapper;
    @Mock
    private BahmniExtensions bahmniExtensions;
    @Mock
    private BahmniConceptService bahmniConceptService;

    private DrugOGramController drugOGramController;

    @Before
    public void setUp() throws Exception {
        drugOGramController = new DrugOGramController(bahmniDrugOrderService, drugOrderToTreatmentRegimenMapper, bahmniConceptService, bahmniExtensions);
        when(bahmniExtensions.getExtension(anyString(), anyString())).thenReturn(new BaseTableExtension());
    }

    @Test
    public void shouldFetchDrugsAsRegimen() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();

        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", null, null, null, null)).thenReturn(drugOrders);

        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, null)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid", null, null);

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", null, null, null, null);

        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, null);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }

    @Test
    public void shouldFetchSpecifiedDrugsAsRegimen() throws Exception {
        Concept paracetemolConcept= new ConceptBuilder().withName("Paracetemol").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(new Date()).withDose(200.0).withConcept(paracetemolConcept).build();

        List<Order> drugOrders = new ArrayList<>();
        drugOrders.add(paracetemol);
        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetemolConcept);
        when(bahmniConceptService.getConceptByFullySpecifiedName("Paracetamol")).thenReturn(paracetemolConcept);

        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", null, concepts, null, null)).thenReturn(drugOrders);

        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid",null, Arrays.asList("Paracetamol"));

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", null, concepts, null, null);

        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, concepts);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }

    @Test
    public void shouldFetchSpecifiedDrugsAsRegimenWhenTheyPassConceptSet() throws Exception {
        Concept paracetamol = new ConceptBuilder().withName("Paracetemol").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();
        Concept tbDrugs = new ConceptBuilder().withName("TB Drugs").withSet(true).withSetMember(paracetamol).build();
        DrugOrder paracetemolDrug = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(new Date()).withDose(200.0).withConcept(paracetamol).build();

        when(bahmniConceptService.getConceptByFullySpecifiedName("TB Drugs")).thenReturn(tbDrugs);
        when(bahmniConceptService.getConceptByFullySpecifiedName("Paracetemol")).thenReturn(paracetamol);

        ArrayList<Order> drugOrders = new ArrayList<>();
        drugOrders.add(paracetemolDrug);
        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetamol);

        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", null, concepts, null, null)).thenReturn(drugOrders);

        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid", null, Arrays.asList("TB Drugs"));

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", null, concepts, null, null);

        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, concepts);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }
}
