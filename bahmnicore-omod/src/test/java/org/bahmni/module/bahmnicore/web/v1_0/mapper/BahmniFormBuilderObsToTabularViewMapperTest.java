package org.bahmni.module.bahmnicore.web.v1_0.mapper;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotRow;
import org.openmrs.module.bahmniemrapi.pivottable.contract.PivotTable;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction.Concept;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BahmniFormBuilderObsToTabularViewMapperTest {

    private BahmniFormBuilderObsToTabularViewMapper bahmniFormBuilderObsToTabularViewMapper;

    @Before
    public void setUp() {
        bahmniFormBuilderObsToTabularViewMapper = new BahmniFormBuilderObsToTabularViewMapper();
    }

    @Test
    public void shouldReturnPivotTableWithEmptyHeadersWhenObservationsAreEmpty() {
        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(Collections.emptySet(),
                Collections.emptyList(), "");

        assertEquals(0, pivotTable.getHeaders().size());
        assertEquals(0, pivotTable.getRows().size());
    }

    @Test
    public void shouldReturnPivotTableWithRowForGivenObservations() {
        String groupByConceptName = "id";
        String weightConceptName = "weight";

        Concept groupByConcept = mock(Concept.class);
        Concept weightConcept = mock(Concept.class);
        when(groupByConcept.getUuid()).thenReturn("group-concept-uuid");
        when(groupByConcept.getName()).thenReturn(groupByConceptName);
        when(weightConcept.getUuid()).thenReturn("weight-concept-uuid");
        when(weightConcept.getName()).thenReturn(weightConceptName);

        BahmniObservation idObservation = mock(BahmniObservation.class);
        BahmniObservation weightObservation = mock(BahmniObservation.class);
        when(idObservation.getConcept()).thenReturn(groupByConcept);
        when(weightObservation.getConcept()).thenReturn(weightConcept);
        when(weightObservation.getValue()).thenReturn("obs value");

        String encounterUuid = "encounter-uuid";
        when(idObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(idObservation.getFormFieldPath()).thenReturn("MedicalForm.10/1-0");
        when(weightObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(weightObservation.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");

        BahmniObservation idObservationLatest = mock(BahmniObservation.class);
        BahmniObservation weightObservationLatest = mock(BahmniObservation.class);
        when(idObservationLatest.getConcept()).thenReturn(groupByConcept);
        when(weightObservationLatest.getConcept()).thenReturn(weightConcept);
        when(weightObservationLatest.getValue()).thenReturn("obs value");

        String encounterUuidLatest = "encounter-uuid-latest";
        when(idObservationLatest.getEncounterUuid()).thenReturn(encounterUuidLatest);
        when(idObservationLatest.getFormFieldPath()).thenReturn("MedicalForm.20/1-0");
        when(weightObservationLatest.getEncounterUuid()).thenReturn(encounterUuidLatest);
        when(weightObservationLatest.getFormFieldPath()).thenReturn("MedicalForm.20/2-0");

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, weightConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, weightObservation, idObservationLatest,
                weightObservationLatest);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);

        assertEquals(2, pivotTable.getHeaders().size());
        assertThat(pivotTable.getHeaders(), containsInAnyOrder(groupByConcept, weightConcept));
        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(2, rows.size());
        Map<String, ArrayList<BahmniObservation>> firstRowColumns = rows.get(0).getColumns();
        assertEquals(2, firstRowColumns.size());
        Map<String, ArrayList<BahmniObservation>> secondRowColumns = rows.get(1).getColumns();
        assertEquals(2, secondRowColumns.size());

        List actualFirstRowObs = asList(firstRowColumns.get(groupByConceptName).get(0),
                firstRowColumns.get(weightConceptName).get(0));
        List actualSecondRowObs = asList(secondRowColumns.get(groupByConceptName).get(0),
                secondRowColumns.get(weightConceptName).get(0));
        List expectedRowOneObs = asList(idObservation, weightObservation);
        List expectedRowTwoObs = asList(idObservationLatest, weightObservationLatest);

        assertTrue(expectedRowOneObs.containsAll(actualFirstRowObs)
                || expectedRowOneObs.containsAll(actualSecondRowObs));
        assertTrue(expectedRowTwoObs.containsAll(actualFirstRowObs)
                || expectedRowTwoObs.containsAll(actualSecondRowObs));
    }

    @Test
    public void shouldReturnPivotTableWithPivotRowsOnlyWhenGroupByConceptObsAvailable() {
        String groupByConceptName = "id";
        String weightConceptName = "weight";

        Concept groupByConcept = mock(Concept.class);
        Concept weightConcept = mock(Concept.class);
        when(groupByConcept.getUuid()).thenReturn("group-concept-uuid");
        when(groupByConcept.getName()).thenReturn(groupByConceptName);
        when(weightConcept.getUuid()).thenReturn("weight-concept-uuid");
        when(weightConcept.getName()).thenReturn(weightConceptName);

        BahmniObservation idObservation = mock(BahmniObservation.class);
        BahmniObservation weightObservation = mock(BahmniObservation.class);
        when(idObservation.getConcept()).thenReturn(groupByConcept);
        when(weightObservation.getConcept()).thenReturn(weightConcept);

        String encounterUuid = "encounter-uuid";
        when(idObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(idObservation.getFormFieldPath()).thenReturn("MedicalForm.10/1-0");
        when(weightObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(weightObservation.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");
        when(weightObservation.getValue()).thenReturn("obs value");

        BahmniObservation anotherWeightObs = mock(BahmniObservation.class);
        when(anotherWeightObs.getConcept()).thenReturn(weightConcept);

        String anotherEncounterUuid = "another-encounter-uuid";
        when(anotherWeightObs.getEncounterUuid()).thenReturn(anotherEncounterUuid);
        when(anotherWeightObs.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");
        when(anotherWeightObs.getValue()).thenReturn("obs value");

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, weightConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, weightObservation, anotherWeightObs);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);

        assertEquals(2, pivotTable.getHeaders().size());
        assertThat(pivotTable.getHeaders(), containsInAnyOrder(groupByConcept, weightConcept));
        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        Map<String, ArrayList<BahmniObservation>> firstRowColumns = rows.get(0).getColumns();
        assertEquals(2, firstRowColumns.size());
        assertEquals(idObservation, firstRowColumns.get(groupByConceptName).get(0));
        assertEquals(weightObservation, firstRowColumns.get(weightConceptName).get(0));
    }

    @Test
    public void shouldGetPivotTableWithOnlyNonNullRows() {
        String groupByConceptName = "id";
        String weightConceptName = "weight";

        Concept groupByConcept = mock(Concept.class);
        Concept weightConcept = mock(Concept.class);
        when(groupByConcept.getUuid()).thenReturn("group-concept-uuid");
        when(groupByConcept.getName()).thenReturn(groupByConceptName);
        when(weightConcept.getUuid()).thenReturn("weight-concept-uuid");
        when(weightConcept.getName()).thenReturn(weightConceptName);

        BahmniObservation idObservation = mock(BahmniObservation.class);
        BahmniObservation weightObservation = mock(BahmniObservation.class);
        when(idObservation.getConcept()).thenReturn(groupByConcept);
        when(weightObservation.getConcept()).thenReturn(weightConcept);
        when(weightObservation.getValue()).thenReturn("obs value");

        String encounterUuid = "encounter-uuid";
        when(idObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(idObservation.getFormFieldPath()).thenReturn("MedicalForm.10/1-0");
        when(weightObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(weightObservation.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");

        BahmniObservation anotherIdObservation = mock(BahmniObservation.class);
        when(anotherIdObservation.getConcept()).thenReturn(groupByConcept);

        String anotherEncounterUuid = "another-encounter-uuid";
        when(anotherIdObservation.getEncounterUuid()).thenReturn(anotherEncounterUuid);
        when(anotherIdObservation.getFormFieldPath()).thenReturn("MedicalForm.10/1-0");

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, weightConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, weightObservation, anotherIdObservation);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);
        assertEquals(2, pivotTable.getHeaders().size());
        assertThat(pivotTable.getHeaders(), containsInAnyOrder(groupByConcept, weightConcept));
        assertEquals(2, pivotTable.getRows().size());

        pivotTable.setRows(bahmniFormBuilderObsToTabularViewMapper.getNonEmptyRows(pivotTable.getRows(),
                groupByConceptName));

        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        Map<String, ArrayList<BahmniObservation>> firstRowColumns = rows.get(0).getColumns();
        assertEquals(2, firstRowColumns.size());
        assertEquals(idObservation, firstRowColumns.get(groupByConceptName).get(0));
        assertEquals(weightObservation, firstRowColumns.get(weightConceptName).get(0));
    }

    @Test
    public void shouldReturnPivotTableForMultiSelectObs() {
        String groupByConceptName = "id";
        String multiSelectConceptName = "speciality";

        Concept groupByConcept = mock(Concept.class);
        Concept multiSelectConcept = mock(Concept.class);
        when(groupByConcept.getUuid()).thenReturn("group-concept-uuid");
        when(groupByConcept.getName()).thenReturn(groupByConceptName);
        when(multiSelectConcept.getUuid()).thenReturn("speciality-concept-uuid");
        when(multiSelectConcept.getName()).thenReturn(multiSelectConceptName);

        BahmniObservation idObservation = mock(BahmniObservation.class);
        when(idObservation.getConcept()).thenReturn(groupByConcept);

        BahmniObservation multiSelectFirstObs = mock(BahmniObservation.class);
        when(multiSelectFirstObs.getConcept()).thenReturn(multiSelectConcept);
        when(multiSelectFirstObs.getValue()).thenReturn("first obs value");

        BahmniObservation multiSelectSecondObs = mock(BahmniObservation.class);
        when(multiSelectSecondObs.getConcept()).thenReturn(multiSelectConcept);
        when(multiSelectSecondObs.getValue()).thenReturn("second obs value");

        String encounterUuid = "encounter-uuid";
        when(idObservation.getEncounterUuid()).thenReturn(encounterUuid);
        when(idObservation.getFormFieldPath()).thenReturn("MedicalForm.10/1-0");
        when(multiSelectFirstObs.getEncounterUuid()).thenReturn(encounterUuid);
        when(multiSelectFirstObs.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");
        when(multiSelectSecondObs.getEncounterUuid()).thenReturn(encounterUuid);
        when(multiSelectSecondObs.getFormFieldPath()).thenReturn("MedicalForm.10/2-0");

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, multiSelectConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, multiSelectFirstObs, multiSelectSecondObs);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);

        assertEquals(2, pivotTable.getHeaders().size());
        assertThat(pivotTable.getHeaders(), containsInAnyOrder(groupByConcept, multiSelectConcept));
        List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        Map<String, ArrayList<BahmniObservation>> firstRowColumns = rows.get(0).getColumns();
        assertEquals(2, firstRowColumns.size());
        assertEquals(idObservation, firstRowColumns.get(groupByConceptName).get(0));
        assertThat(firstRowColumns.get(multiSelectConceptName),
                containsInAnyOrder(multiSelectFirstObs, multiSelectSecondObs));
    }

    @Test
    public void shouldReturnPivotTableWithTwoRowsDifferentiatedByEncounterUUIDAndParentFormFieldPathsWhenAddMoreSectionHasAllConceptsIncludingGroupByConcept() {

        String groupByConceptName = "id";
        String weightConceptName = "weight";

        Concept groupByConcept = new Concept();
        Concept weightConcept = new Concept();
        groupByConcept.setUuid("group-concept-uuid");
        groupByConcept.setName(groupByConceptName);
        weightConcept.setUuid("weight-concept-uuid");
        weightConcept.setName(weightConceptName);

        BahmniObservation idObservation = new BahmniObservation();
        BahmniObservation weightObservation = new BahmniObservation();
        idObservation.setConcept(groupByConcept);
        weightObservation.setConcept(weightConcept);
        weightObservation.setValue("obs value");
        idObservation.setValue("1");
        String encounterUuid = "encounter-uuid";
        idObservation.setEncounterUuid(encounterUuid);
        idObservation.setFormFieldPath("MedicalForm.10/1-0/2-0");
        weightObservation.setEncounterUuid(encounterUuid);
        weightObservation.setFormFieldPath("MedicalForm.10/1-0/3-0");

        BahmniObservation anotherIdObservation = new BahmniObservation();
        BahmniObservation anotherWeightObservation = new BahmniObservation();
        anotherIdObservation.setConcept(groupByConcept);
        anotherWeightObservation.setConcept(weightConcept);
        anotherWeightObservation.setValue("another obs value");
        anotherIdObservation.setValue(1);
        anotherIdObservation.setEncounterUuid(encounterUuid);
        anotherIdObservation.setFormFieldPath("MedicalForm.10/1-1/2-0");
        anotherWeightObservation.setUuid(encounterUuid);
        anotherWeightObservation.setFormFieldPath("MedicalForm.10/1-1/3-0");
        anotherIdObservation.setEncounterUuid(encounterUuid);
        anotherWeightObservation.setEncounterUuid(encounterUuid);

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, weightConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, weightObservation, anotherIdObservation,
                anotherWeightObservation);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);

        assertEquals(2, pivotTable.getHeaders().size());
        final List<PivotRow> rows = pivotTable.getRows();
        assertEquals(2, rows.size());
        assertEquals(2, rows.get(0).getColumns().size());
        assertEquals(2, rows.get(1).getColumns().size());

        final Map<String, ArrayList<BahmniObservation>> firstColumn = rows.get(0).getColumns();
        final Map<String, ArrayList<BahmniObservation>> secondColumn = rows.get(1).getColumns();

        final List<Object> actualFirstRow = asList(firstColumn.get(groupByConceptName).get(0),
                firstColumn.get(weightConceptName).get(0));

        final List<Object> actualSecondRow = asList(secondColumn.get(groupByConceptName).get(0),
                secondColumn.get(weightConceptName).get(0));

        List expectedFirstRow = asList(idObservation, weightObservation);
        List expectedSecondRow = asList(anotherIdObservation, anotherWeightObservation);


        assertTrue(expectedFirstRow.containsAll(actualFirstRow)
                || expectedFirstRow.containsAll(actualSecondRow));
        assertTrue(expectedSecondRow.containsAll(actualFirstRow)
                || expectedSecondRow.containsAll(actualSecondRow));
    }

    @Test
    public void shouldReturnPivotTableWithOneRowWhenAddMoreSectionHasAllConceptsExceptGroupByConcept() {
        String groupByConceptName = "id";
        String weightConceptName = "weight";

        Concept groupByConcept = new Concept();
        Concept weightConcept = new Concept();
        groupByConcept.setUuid("group-concept-uuid");
        groupByConcept.setName(groupByConceptName);
        weightConcept.setUuid("weight-concept-uuid");
        weightConcept.setName(weightConceptName);

        BahmniObservation idObservation = new BahmniObservation();
        BahmniObservation weightObservation = new BahmniObservation();
        idObservation.setConcept(groupByConcept);
        weightObservation.setConcept(weightConcept);
        weightObservation.setValue("obs value");
        idObservation.setValue("1");
        String encounterUuid = "encounter-uuid";
        idObservation.setEncounterUuid(encounterUuid);
        idObservation.setFormFieldPath("MedicalForm.10/1-0");
        weightObservation.setEncounterUuid(encounterUuid);
        weightObservation.setFormFieldPath("MedicalForm.10/2-0/3-0");

        BahmniObservation anotherWeightObservation = new BahmniObservation();
        anotherWeightObservation.setConcept(weightConcept);
        anotherWeightObservation.setValue("another obs value");
        anotherWeightObservation.setUuid(encounterUuid);
        anotherWeightObservation.setFormFieldPath("MedicalForm.10/2-1/3-0");
        anotherWeightObservation.setEncounterUuid(encounterUuid);

        HashSet<Concept> concepts = new HashSet<>(asList(groupByConcept, weightConcept));
        List<BahmniObservation> bahmniObservations = asList(idObservation, weightObservation,
                anotherWeightObservation);

        PivotTable pivotTable = bahmniFormBuilderObsToTabularViewMapper.constructTable(concepts, bahmniObservations,
                groupByConceptName);

        assertEquals(2, pivotTable.getHeaders().size());
        final List<PivotRow> rows = pivotTable.getRows();
        assertEquals(1, rows.size());
        assertEquals(2, rows.get(0).getColumns().size());

        final Map<String, ArrayList<BahmniObservation>> columns = rows.get(0).getColumns();

        final List<Object> actualRow = asList(columns.get(groupByConceptName).get(0),
                columns.get(weightConceptName).get(0), columns.get(weightConceptName).get(1));

        List expectedRow = asList(idObservation, weightObservation, anotherWeightObservation);


        assertTrue(expectedRow.containsAll(actualRow));
    }
}
