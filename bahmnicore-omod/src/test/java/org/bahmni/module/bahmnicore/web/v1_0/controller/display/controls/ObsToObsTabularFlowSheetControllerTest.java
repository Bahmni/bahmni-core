package org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls;

import org.bahmni.module.bahmnicore.extensions.BahmniExtensions;
import org.bahmni.module.bahmnicore.service.BahmniConceptService;
import org.bahmni.module.bahmnicore.service.BahmniObsService;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniFormBuilderObsToTabularViewMapper;
import org.bahmni.module.bahmnicore.web.v1_0.mapper.BahmniObservationsToTabularViewMapper;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Concept;
import org.openmrs.ConceptNumeric;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.TestUsernameAuthenticationScheme;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.drugogram.contract.BaseTableExtension;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.ConceptMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class ObsToObsTabularFlowSheetControllerTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private BahmniObservationsToTabularViewMapper bahmniObservationsToTabularViewMapper;
    @Mock
    private ConceptService conceptService;
    @Mock
    private BahmniObsService bahmniObsService;
    @Mock
    private BahmniExtensions bahmniExtensions;
    @Mock
    private BahmniConceptService bahmniConceptService;
    @Mock
    private BahmniFormBuilderObsToTabularViewMapper bahmniFormBuilderObsToTabularViewMapper;

    @Captor
    private ArgumentCaptor<Set<EncounterTransaction.Concept>> leafConceptsCaptured;

    private ObsToObsTabularFlowSheetController obsToObsPivotTableController;
    private ConceptMapper conceptMapper = new ConceptMapper();

    private MockedStatic<LocaleUtility> localeUtilityMockedStatic;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        localeUtilityMockedStatic = Mockito.mockStatic(LocaleUtility.class);
        localeUtilityMockedStatic.when(LocaleUtility::getDefaultLocale).thenReturn(Locale.ENGLISH);
        Context.setUserContext(new UserContext(new TestUsernameAuthenticationScheme()));
        obsToObsPivotTableController = new ObsToObsTabularFlowSheetController(bahmniObsService, conceptService,
                bahmniObservationsToTabularViewMapper, bahmniExtensions, bahmniConceptService,
                bahmniFormBuilderObsToTabularViewMapper);
    }

    @After
    public void tearDown() {
        localeUtilityMockedStatic.close();
    }

    // NOTE: The two tests below used to stub/verify the PRIVATE methods getPivotTableByConceptSet/getPivotTableByFormNames
    // directly via PowerMockito's spy()/doReturn()/verifyPrivate(...), which has no vanilla Mockito equivalent (private methods
    // cannot be stubbed/verified without PowerMock's bytecode instrumentation). Since constructPivotTableFor's dispatch between
    // these two private methods is a simple if/else on `conceptSet != null`, and each private method is uniquely characterized by
    // which collaborator it calls (getPivotTableByConceptSet -> bahmniObservationsToTabularViewMapper; getPivotTableByFormNames ->
    // bahmniFormBuilderObsToTabularViewMapper/bahmniObsService.getObsForFormBuilderForms), we verify the same dispatch behavior
    // observably through those collaborator interactions instead - an equivalent, behavior-level substitute for verifyPrivate.
    @Test
    public void shouldCallGetPivotTableByConceptSetMethodWhenConceptSetIsGiven() throws Exception {
        String conceptSetName = "ConceptSetName";
        String groupByConceptName = "GroupByConcept";
        Concept groupByConcept = new ConceptBuilder().withName(groupByConceptName).withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName(conceptSetName).withSetMember(groupByConcept).withDataType("Numeric").withSet(true).withClass("ConvSet").build();
        when(conceptService.getConceptByName(conceptSetName)).thenReturn(rootConcept);
        when(conceptService.getConceptByName(groupByConceptName)).thenReturn(groupByConcept);
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor(any(), any(Concept.class), any(Concept.class), any(), any(), any(), any())).thenReturn(bahmniObservations);
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(new PivotTable());

        obsToObsPivotTableController.constructPivotTableFor(null, null,
                conceptSetName, groupByConceptName, null, null,
                null, null, null, null, null,
                null, null);

        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), eq(groupByConceptName));
        verify(bahmniObsService, never()).getObsForFormBuilderForms(any(), any(), any(), any(), any(), any());
        verifyNoInteractions(bahmniFormBuilderObsToTabularViewMapper);
    }

    @Test
    public void shouldCallGetPivotTableByFormNamesSetMethodWhenConceptNamesAndFormNamesGiven() throws Exception {
        String groupByConceptName = "GroupByConcept";
        List<String> conceptNames = new ArrayList<>(Arrays.asList("conceptOne", "conceptTwo"));
        List<String> formNames = Arrays.asList("FormOne", "FormTwo");

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.getObsForFormBuilderForms(any(), any(), any(), any(), any(), any())).thenReturn(bahmniObservations);
        List<Concept> concepts = Arrays.asList(
                new ConceptBuilder().withName("conceptOne").withSet(false).withDataType("Numeric").build(),
                new ConceptBuilder().withName("conceptTwo").withSet(false).withDataType("Numeric").build(),
                new ConceptBuilder().withName(groupByConceptName).withSet(false).withDataType("Numeric").build());
        when(bahmniConceptService.getConceptsByFullySpecifiedName(any())).thenReturn(concepts);
        PivotTable pivotTable = new PivotTable();
        when(bahmniFormBuilderObsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), eq(groupByConceptName))).thenReturn(pivotTable);
        when(bahmniFormBuilderObsToTabularViewMapper.getNonEmptyRows(pivotTable.getRows(), groupByConceptName)).thenReturn(pivotTable.getRows());

        obsToObsPivotTableController.constructPivotTableFor(null, null,
                null, groupByConceptName, null, conceptNames,
                null, null, null, null, null,
                null, formNames);

        verify(bahmniFormBuilderObsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), eq(groupByConceptName));
        verify(bahmniObsService, never()).observationsFor(any(), any(Concept.class), any(Concept.class), any(), any(), any(), any());
        verifyNoInteractions(bahmniObservationsToTabularViewMapper);
    }

    @Test
    public void shouldReturnEmptyPivotTableWhenFormNamesAreNotGiven() throws ParseException {

        ObsToObsTabularFlowSheetController obsToObsTabularFlowSheetController = spy(obsToObsPivotTableController);
        PivotTable pivotTable = obsToObsTabularFlowSheetController.constructPivotTableFor(any(), any(), any(), any(), any(),
                eq(singletonList("")), any(), any(), any(), any(), any(), any(), eq(null));
        assertEquals(0, pivotTable.getHeaders().size());
    }

    @Test
    public void shouldReturnEmptyPivotTableWhenConceptNamesAreNotGiven() throws ParseException {

        ObsToObsTabularFlowSheetController obsToObsTabularFlowSheetController = spy(obsToObsPivotTableController);
        PivotTable pivotTable = obsToObsTabularFlowSheetController.constructPivotTableFor(any(), any(), any(), any(), any(),
                eq(null), any(), any(), any(), any(), any(), any(), eq(singletonList("")));

        assertEquals(0, pivotTable.getHeaders().size());
    }

    @Test
    public void shouldReturnEmptyPivotTableWhenFormNamesAreEmpty() throws ParseException {

        ObsToObsTabularFlowSheetController obsToObsTabularFlowSheetController = spy(obsToObsPivotTableController);
        PivotTable pivotTable = obsToObsTabularFlowSheetController.constructPivotTableFor(any(), any(), any(), any(), any(),
                eq(null), any(), any(), any(), any(), any(), any(), eq(emptyList()));

        assertEquals(0, pivotTable.getHeaders().size());
    }

    @Test
    public void shouldFetchObservationForSpecifiedConceptsAndGroupByConcept() throws ParseException {
        Concept member1 = new ConceptBuilder().withName("Member1").withSet(false).withDataType("Numeric").build();
        Concept member2 = new ConceptBuilder().withName("Member2").withSet(false).withDataType("Numeric").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withSet(false).withDataType("Numeric").build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withSetMember(member1).withSetMember(member2).withSet(true).withDataType("Numeric").build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("Member1", "Member2");
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1,
                "ConceptSetName", "GroupByConcept", null, conceptNames,null, null, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        verify(bahmniExtensions,times(0)).getExtension(anyString(),anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldFetchObservationForSpecifiedFormNamesAndGroupByConcept() throws ParseException {
        Concept member1 = new ConceptBuilder().withName("Member1").withSet(false).withDataType("Numeric").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withSet(false).withDataType("Numeric").build();
        String groupByConceptName = "GroupByConcept";
        List<String> formNames = singletonList("Form1");

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.getObsForFormBuilderForms("patientUuid", formNames, 1, null, null, null)).thenReturn(bahmniObservations);

        List<String> conceptNames = new ArrayList<>(singletonList("Member1"));
        List<Concept> concepts = Arrays.asList(member1, groupByConcept);
        when(bahmniConceptService.getConceptsByFullySpecifiedName(conceptNames)).thenReturn(concepts);

        PivotTable pivotTable = new PivotTable();
        when(bahmniFormBuilderObsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(),
                eq(bahmniObservations), eq(groupByConceptName))).thenReturn(pivotTable);
        when(bahmniFormBuilderObsToTabularViewMapper.getNonEmptyRows(pivotTable.getRows(), groupByConceptName)).thenReturn(pivotTable.getRows());

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1,
                null, groupByConceptName, null, conceptNames,null, null,
                null, null, null, null, formNames);

        verify(bahmniObsService).getObsForFormBuilderForms("patientUuid", formNames, 1, null, null, null);
        conceptNames.addAll(singletonList(groupByConceptName));
        verify(bahmniConceptService).getConceptsByFullySpecifiedName(conceptNames);
        verify(bahmniFormBuilderObsToTabularViewMapper).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(),
                eq(bahmniObservations), eq(groupByConceptName));
        verify(bahmniExtensions,times(0)).getExtension(anyString(),anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldFetchSpecifiedConceptSetsData() throws Exception {
        Concept member1 = new ConceptBuilder().withName("Member1").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept member2 = new ConceptBuilder().withName("Member2").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept parent = new ConceptBuilder().withName("Parent").withSetMember(member1).withSetMember(member2).withSet(true).withClass("N/A").withDataType("N/A").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withSetMember(parent).withClass("N/A").withDataType("Numeric").withSet(true).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();

        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("Parent");

        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1,
                "ConceptSetName", "GroupByConcept", null, conceptNames, null, null, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(conceptService, times(1)).getConceptByName("GroupByConcept");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());

        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldFetchAllVisitsDataIfNumberOfVisitsIsPassedAsNull() throws Exception {
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withDataType("Numeric").withSet(true).build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, null, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("GroupByConcept");

        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", null, "ConceptSetName", "GroupByConcept", null, conceptNames, null, null, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, null, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldFetchAllVisitsDataIfNumberOfVisitsIsPassedAsZero() throws Exception {
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withClass("N/A").withSetMember(groupByConcept).withDataType("Numeric").withSet(true).build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 0, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 0, "ConceptSetName", "GroupByConcept", null, null, null, null, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 0, null , null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldFetchAllVisitsDataIfNumberOfVisitsIsPassedAsNegative() throws Exception {
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withClass("N/A").withSetMember(groupByConcept).withDataType("Numeric").withSet(true).build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, -1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", -1, "ConceptSetName", "GroupByConcept", null, null, null, null, null, null, null, null, null);
        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, -1, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldThrowExceptionIfConceptSetNotFound() throws ParseException {
        String conceptSetName = "ConceptSetName";
        when(conceptService.getConceptByName(conceptSetName)).thenReturn(null);
        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("Root concept not found for the name:  " + conceptSetName));

        obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, conceptSetName, "GroupByConcept", null, Collections.EMPTY_LIST, null, null, null, null, null, null, null);
    }

    @Test
    public void shouldThrowExceptionIfGroupByConceptIsNotProvided() throws ParseException {
        String conceptSetName = "ConceptSetName";
        Concept conceptSet = new ConceptBuilder().withName(conceptSetName).withSetMember(new ConceptBuilder().withName("GroupByConcept").build()).build();
        when(conceptService.getConceptByName(conceptSetName)).thenReturn(conceptSet);
        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("null doesn't belong to the Root concept:  " + conceptSetName));

        obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, conceptSetName, null, null, Collections.EMPTY_LIST, null, null, null, null, null, null, null);
    }

    @Test
    public void shouldThrowExceptionIfGroupByConceptDoesNotBelongToConceptSet() throws ParseException {
        String conceptSetName = "ConceptSetName";
        Concept conceptSet = new ConceptBuilder().withName(conceptSetName).withSetMember(new ConceptBuilder().withName("NotGroupByConcept").build()).build();
        when(conceptService.getConceptByName(conceptSetName)).thenReturn(conceptSet);
        exception.expect(RuntimeException.class);
        exception.expectMessage(containsString("GroupByConcept doesn't belong to the Root concept:  " + conceptSetName));

        obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, conceptSetName, "GroupByConcept", null, Collections.EMPTY_LIST, null, null, null, null, null, null, null);
    }

    @Test
    public void shouldFetchTheRequiredNoOfObservationsWhenInitialCountAndLatestCountAreGiven() throws Exception {
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withClass("N/A").withSetMember(groupByConcept).withDataType("Numeric").withSet(true).build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, -1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", -1, "ConceptSetName", "GroupByConcept", null,  null, bahmniObservations.size(), 1, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, -1, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldSortTheConceptsAsTheOrderDefinedIntheConceptNames() throws Exception {
        Concept member1 = new ConceptBuilder().withName("Member1").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept member2 = new ConceptBuilder().withName("Member2").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept parent = new ConceptBuilder().withName("Parent").withSetMember(member1).withSetMember(member2).withSet(true).withClass("N/A").withDataType("N/A").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withClass("N/A").withDataType("Numeric").withSet(false).build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withSetMember(parent).withClass("N/A").withDataType("Numeric").withSet(true).build();
        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();

        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("Member2", "Member1");

//        Set<String> leafConcepts = new HashSet<>(Arrays.asList("Member1", "Member2", "GroupByConcept"));
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, "ConceptSetName", "GroupByConcept", null, conceptNames, null, null, null, null, null, null, null);

        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(conceptService, times(1)).getConceptByName("GroupByConcept");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null);

        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(leafConceptsCaptured.capture(), eq(bahmniObservations), anyString());
        Set<EncounterTransaction.Concept> actualLeafConcepts = leafConceptsCaptured.getValue();

        Iterator<EncounterTransaction.Concept> iterator = actualLeafConcepts.iterator();
        assertEquals(iterator.next().getName(), "Member2");
        assertEquals(iterator.next().getName(), "Member1");

        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);
    }

    @Test
    public void shouldPassPatientProgramUuidToObsService() throws ParseException {
        String patientProgramUuid = "auspiciousUuid";
        Concept member1 = new ConceptBuilder().withName("Member1").withSet(false).withDataType("Numeric").build();
        Concept member2 = new ConceptBuilder().withName("Member2").withSet(false).withDataType("Numeric").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withSet(false).withDataType("Numeric").build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withSetMember(member1).withSetMember(member2).withSet(true).withDataType("Numeric").build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, patientProgramUuid)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("Member1", "Member2");
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, "ConceptSetName", "GroupByConcept", null, conceptNames,null, null, null, null, null, patientProgramUuid, null);

        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, patientProgramUuid);

    }

    @Test
    public void shouldFetchPivotTableWithHighNormalLowNormalAndUnitsForConceptWithConceptDetailsClass() throws Exception {
        Concept labMagnesium = new ConceptBuilder().withName("Lab, Magnesium").withClass("Misc").withSet(false).withDataTypeNumeric().build();
        Concept labMagnesiumAbnormal = new ConceptBuilder().withName("Lab, Magnesium Abnormal").withClass("Abnormal").withDataType("Boolean").withSet(false).build();
        Concept labMagnesiumData = new ConceptBuilder().withName("Lab, Magnesium Data").withClass("Concept Details").withDataType("N/A").withSetMember(labMagnesium).withSetMember(labMagnesiumAbnormal).withSet(true).build();
        ConceptNumeric labMagnesiumNumeric = new ConceptNumeric();
        labMagnesiumNumeric.setHiNormal(5.0);
        labMagnesiumNumeric.setLowNormal(2.0);
        labMagnesiumNumeric.setUnits("mg");
        when(conceptService.getConceptNumeric(eq(null))).thenReturn(labMagnesiumNumeric);

        when(conceptService.getConceptByName("Lab, Magnesium Data")).thenReturn(labMagnesiumData);
        when(conceptService.getConceptByName("Lab, Magnesium")).thenReturn(labMagnesium);
        when(conceptService.getConceptByName("Lab, Magnesium Abnormal")).thenReturn(labMagnesiumAbnormal);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        bahmniObservations.add(new BahmniObservation().setConcept(conceptMapper.map(labMagnesium)).setValue(2.3));
        when(bahmniObsService.observationsFor("patientUuid", labMagnesiumData, labMagnesium, -1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        Set<EncounterTransaction.Concept> headers = new HashSet<>();

        headers.add(conceptMapper.map(labMagnesiumData));
        pivotTable.setHeaders(headers);
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);
        when(conceptService.getConceptsByConceptSet(conceptService.getConceptByUuid(anyString()))).thenReturn(Arrays.asList(labMagnesium,labMagnesiumAbnormal));
        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", -1, "Lab, Magnesium Data", "Lab, Magnesium", null, Arrays.asList("Lab, Magnesium"), bahmniObservations.size(), 1, null, null, null, null, null);
        Set<EncounterTransaction.Concept> actualHeaders = actualPivotTable.getHeaders();
        EncounterTransaction.Concept header = actualHeaders.iterator().next();
        assertEquals(new Double(2.0), header.getLowNormal());
        assertEquals(new Double(5.0), header.getHiNormal());
        assertEquals("mg", header.getUnits());
    }

    @Test
    public void ensureThatExtensionsAreNotLoadedWhenNameIsNotProvided() throws ParseException {
        Concept member1 = new ConceptBuilder().withName("Member1").withSet(false).withDataType("Numeric").build();
        Concept member2 = new ConceptBuilder().withName("Member2").withSet(false).withDataType("Numeric").build();
        Concept groupByConcept = new ConceptBuilder().withName("GroupByConcept").withSet(false).withDataType("Numeric").build();
        Concept rootConcept = new ConceptBuilder().withName("ConceptSetName").withSetMember(groupByConcept).withSetMember(member1).withSetMember(member2).withSet(true).withDataType("Numeric").build();
        when(conceptService.getConceptByName("ConceptSetName")).thenReturn(rootConcept);
        when(conceptService.getConceptByName("GroupByConcept")).thenReturn(groupByConcept);

        ArrayList<BahmniObservation> bahmniObservations = new ArrayList<>();
        when(bahmniObsService.observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = new PivotTable();
        List<String> conceptNames = Arrays.asList("Member1", "Member2");
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, "ConceptSetName", "GroupByConcept", null, conceptNames,null, null, "groovyFileName", null, null, null, null);
        Mockito.when(bahmniExtensions.getExtension(ObsToObsTabularFlowSheetController.FLOWSHEET_EXTENSION, "groovyFileName.groovy")).thenReturn(new BaseTableExtension());


        verify(conceptService, times(1)).getConceptByName("ConceptSetName");
        verify(bahmniObsService, times(1)).observationsFor("patientUuid", rootConcept, groupByConcept, 1, null, null, null);
        verify(bahmniObservationsToTabularViewMapper, times(1)).constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString());
        assertNotNull(actualPivotTable);
        assertEquals(pivotTable, actualPivotTable);


    }

    @Test
    public void shouldReturnOrderedPivotTableWhenOrderByConceptGiven() throws ParseException {
        Concept saeTerm = new ConceptBuilder().withName("SAE Term").withDataType("Text").build();
        Concept saeDate = new ConceptBuilder().withName("SAE Date").withDataType("Date").build();
        Concept template = new ConceptBuilder().withName("SAE Template").withSet(true).withSetMember(saeTerm).withSetMember(saeDate).build();
        when(conceptService.getConceptByName("SAE Template")).thenReturn(template);
        when(conceptService.getConceptByName("SAE Term")).thenReturn(saeTerm);
        when(conceptService.getConceptByName("SAE Date")).thenReturn(saeDate);

        Map<Concept, String> conceptToValueMap1 = new HashMap<>();
        conceptToValueMap1.put(saeTerm, "c");
        conceptToValueMap1.put(saeDate, "2016-01-11");
        Map<Concept, String> conceptToValueMap2 = new HashMap<>();
        conceptToValueMap2.put(saeTerm, "a");
        conceptToValueMap2.put(saeDate, "2016-01-01");
        Map<Concept, String> conceptToValueMap3 = new HashMap<>();
        conceptToValueMap3.put(saeTerm, "d");
        conceptToValueMap3.put(saeDate, "2016-01-05");
        List<BahmniObservation> bahmniObservations = buildBahmniObservations(Arrays.asList(conceptToValueMap1, conceptToValueMap2, conceptToValueMap3));

        when(bahmniObsService.observationsFor("patientUuid", template, saeTerm, 1, null, null, null)).thenReturn(bahmniObservations);

        PivotTable pivotTable = pivotTableBuilder(Arrays.asList(saeTerm, saeDate), bahmniObservations);
        when(bahmniObservationsToTabularViewMapper.constructTable(ArgumentMatchers.<Set<EncounterTransaction.Concept>>any(), eq(bahmniObservations), anyString())).thenReturn(pivotTable);

        PivotTable actualPivotTable = obsToObsPivotTableController.constructPivotTableFor("patientUuid", 1, "SAE Template", "SAE Term", "SAE Term", Arrays.asList("SAE Date"), null, null, null, null, null, null, null);
        assertEquals(actualPivotTable.getRows().get(0).getColumns().get("SAE Term").get(0).getValue(), "a");
        assertEquals(actualPivotTable.getRows().get(1).getColumns().get("SAE Term").get(0).getValue(), "c");
        assertEquals(actualPivotTable.getRows().get(2).getColumns().get("SAE Term").get(0).getValue(), "d");
    }

    private PivotTable pivotTableBuilder(List<Concept> concepts, List<BahmniObservation> bahmniObservations) {
        PivotTable pivotTable = new PivotTable();
        Set<EncounterTransaction.Concept> headers = new HashSet<>();
        for (Concept concept:concepts) {
            headers.add(conceptMapper.map(concept));
        }
        List<PivotRow> pivotRows = new ArrayList<>();
        for(BahmniObservation bahmniObservation : bahmniObservations) {
            PivotRow pivotRow = new PivotRow();
            Collection<BahmniObservation> groupMembers = bahmniObservation.getGroupMembers();
            for(BahmniObservation groupMember: groupMembers) {
                pivotRow.addColumn(groupMember.getConcept().getName(), groupMember);
            }
            pivotRows.add(pivotRow);
        }
        pivotTable.setHeaders(headers);
        pivotTable.setRows(pivotRows);
        return pivotTable;
    }

    private List<BahmniObservation> buildBahmniObservations(List<Map<Concept, String>> conceptToValueMaps) {
        List<BahmniObservation> bahmniObservations = new ArrayList<>();
        for(Map<Concept, String> conceptToValueMap : conceptToValueMaps) {
            BahmniObservation bahmniObservation = new BahmniObservation();
            for(Entry<Concept, String> entry : conceptToValueMap.entrySet()) {
                BahmniObservation observation = new BahmniObservation();
                observation.setConcept(conceptMapper.map(entry.getKey()));
                observation.setType(entry.getKey().getDatatype().getName());
                observation.setValue(entry.getValue());
                bahmniObservation.addGroupMember(observation);
            }
            bahmniObservations.add(bahmniObservation);
        }
        return bahmniObservations;
    }

}
