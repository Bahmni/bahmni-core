package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.DrugOrderToTreatmentRegimenMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.bahmni.test.builder.DrugOrderBuilder;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.DrugOrder;
import org.openmrs.Order;
import org.openmrs.api.ConceptService;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.drugogram.contract.TreatmentRegimen;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
public class DrugOGramControllerTest {
    @Mock
    private BahmniDrugOrderService bahmniDrugOrderService;
    @Mock
    private DrugOrderToTreatmentRegimenMapper drugOrderToTreatmentRegimenMapper;
    @Mock
    private ConceptService conceptService;
    @Mock
    private BahmniExtensions bahmniExtensions;

    private DrugOGramController drugOGramController;

    @Before
    public void setUp() throws Exception {
        drugOGramController = new DrugOGramController(bahmniDrugOrderService, drugOrderToTreatmentRegimenMapper, conceptService,bahmniExtensions);
        when(bahmniExtensions.getExtension(anyString())).thenReturn(new BaseTableExtension());
    }

    @Test
    public void shouldFetchDrugsAsRegimen() throws Exception {
        ArrayList<Order> drugOrders = new ArrayList<>();
        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", null, null, null, null)).thenReturn(drugOrders);
        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, null)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid", null, null, null);

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", null, null, null, null);
        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, null);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }

    @Test
    public void shouldFetchSpecifiedDrugsAsRegimen() throws Exception {
        Concept paracetemolConcept= new ConceptBuilder().withName("Paracetemol").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();;
        DrugOrder paracetemol = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(new Date()).withDose(200.0).withConcept(paracetemolConcept).build();

        List<Order> drugOrders = new ArrayList<>();
        drugOrders.add(paracetemol);
        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetemolConcept);
        when(conceptService.getConceptByName("Paracetamol")).thenReturn(paracetemolConcept);

        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", concepts, null, null, null)).thenReturn(drugOrders);
        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid", Arrays.asList("Paracetamol"), null, null);

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", concepts, null, null, null);
        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, concepts);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }

    @Test
    public void shouldFetchSpecifiedDrugsAsRegimenWhenTheyPassConceptSet() throws Exception {
        Concept paracetamol = new ConceptBuilder().withName("Paracetemol").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("Paracetemol").build();;
        Concept tbDrugs = new ConceptBuilder().withName("TB Drugs").withSet(true).withSetMember(paracetamol).build();
        DrugOrder paracetemolDrug = new DrugOrderBuilder().withDrugName("Paracetemol").withDateActivated(new Date()).withDose(200.0).withConcept(paracetamol).build();

        when(conceptService.getConceptByName("TB Drugs")).thenReturn(tbDrugs);
        when(conceptService.getConceptByName("Paracetemol")).thenReturn(paracetamol);

        ArrayList<Order> drugOrders = new ArrayList<>();
        drugOrders.add(paracetemolDrug);
        Set<Concept> concepts = new LinkedHashSet<>();
        concepts.add(paracetamol);
        when(bahmniDrugOrderService.getAllDrugOrders("patientUuid", concepts, null, null, null)).thenReturn(drugOrders);
        TreatmentRegimen expected = new TreatmentRegimen();
        when(drugOrderToTreatmentRegimenMapper.map(drugOrders, concepts)).thenReturn(expected);

        TreatmentRegimen actual = drugOGramController.getRegimen("patientUuid", Arrays.asList("TB Drugs"), null, null);

        verify(bahmniDrugOrderService, times(1)).getAllDrugOrders("patientUuid", concepts, null, null, null);
        verify(drugOrderToTreatmentRegimenMapper, times(1)).map(drugOrders, concepts);
        assertEquals(expected, actual);
        assertEquals(0, expected.getHeaders().size());
    }
}
