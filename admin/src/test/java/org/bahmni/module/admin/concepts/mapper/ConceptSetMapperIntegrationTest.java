package org.bahmni.module.admin.concepts.mapper;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.bahmni.module.referencedata.labconcepts.contract.ConceptSet;
import org.bahmni.test.builder.ConceptBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConceptSetMapperIntegrationTest {
    public static final String SAME_AS = "SAME-AS";
    private ConceptSetMapper conceptSetMapper;
    private ArrayList<KeyValue> children;
    private org.bahmni.module.referencedata.labconcepts.mapper.ConceptSetMapper referenceDataConceptSetMapper;

    @Before
    public void setUp() throws Exception {
        conceptSetMapper = new ConceptSetMapper();
        referenceDataConceptSetMapper = new org.bahmni.module.referencedata.labconcepts.mapper.ConceptSetMapper();
        children = new ArrayList<>();
        children.add(new KeyValue("1", "child1"));
        children.add(new KeyValue("2", "child2"));
    }

    @Test
    public void mapConceptSetRowToConceptSetDto() throws Exception {
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.name = "UniqueName";
        conceptSetRow.shortName = "shortName";
        conceptSetRow.conceptClass = "ConvSet";
        conceptSetRow.children = children;
        conceptSetRow.uuid = UUID.randomUUID().toString();

        ConceptSet conceptSet = conceptSetMapper.map(conceptSetRow);
        assertEquals(2, conceptSet.getChildren().size());
        assertEquals(conceptSetRow.name, conceptSet.getUniqueName());
        assertEquals(conceptSetRow.shortName, conceptSet.getDisplayName());
        assertEquals(conceptSetRow.conceptClass, conceptSet.getClassName());
        assertEquals(conceptSetRow.getUuid(), conceptSet.getUuid());
    }

    @Test
    public void nullIfNoShortName() throws Exception {
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.name = "Some";
        conceptSetRow.shortName = " ";
        ConceptSet map = conceptSetMapper.map(conceptSetRow);
        assertNull(map.getDisplayName());
        assertEquals(conceptSetRow.name, map.getUniqueName());
    }

    @Test
    public void shouldNotMapEmptyChildren() throws Exception {
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.name = "UniqueName";
        conceptSetRow.shortName = "shortName";
        conceptSetRow.conceptClass = "ConvSet";
        conceptSetRow.children = children;
        conceptSetRow.children.add(new KeyValue("3", ""));

        ConceptSet conceptSet = conceptSetMapper.map(conceptSetRow);
        assertEquals(2, conceptSet.getChildren().size());
        assertEquals(conceptSetRow.name, conceptSet.getUniqueName());
        assertEquals(conceptSetRow.shortName, conceptSet.getDisplayName());
        assertEquals(conceptSetRow.conceptClass, conceptSet.getClassName());

    }

    @Test
    public void mapConceptReferenceTermToConceptSetDto() throws Exception {
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.name = "UniqueName";
        conceptSetRow.shortName = "shortName";
        conceptSetRow.conceptClass = "ConvSet";
        conceptSetRow.children = children;
        ConceptReferenceTermRow conceptReferenceTermRow = new ConceptReferenceTermRow( "org.openmrs.module.emrapi","New Code", SAME_AS);
        List<ConceptReferenceTermRow> conceptReferenceTermsList = new ArrayList<>(Arrays.asList(conceptReferenceTermRow));
        conceptSetRow.referenceTerms = conceptReferenceTermsList;

        ConceptSet conceptSet = conceptSetMapper.map(conceptSetRow);
        assertEquals(2, conceptSet.getChildren().size());
        assertEquals(conceptSetRow.name, conceptSet.getUniqueName());
        assertEquals(conceptSetRow.shortName, conceptSet.getDisplayName());
        assertEquals(conceptSetRow.conceptClass, conceptSet.getClassName());
        assertEquals("New Code", conceptSet.getConceptReferenceTermsList().get(0).getReferenceTermCode());
        assertEquals(SAME_AS, conceptSet.getConceptReferenceTermsList().get(0).getReferenceTermRelationship());
        assertEquals("org.openmrs.module.emrapi", conceptSet.getConceptReferenceTermsList().get(0).getReferenceTermSource());
    }

    @Test
    public void getConceptListInOrderFromConceptSet() throws Exception {
        org.openmrs.Concept child1 = new ConceptBuilder().withName("Child1").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("short").build();
        org.openmrs.Concept child2 = new ConceptBuilder().withName("Child2").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("short").build();
        org.openmrs.Concept child3 = new ConceptBuilder().withName("Child3").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("short").build();
        org.openmrs.Concept child4 = new ConceptBuilder().withName("Child4").withDescription("Description").withClass("Some").withDataType("N/A").withShortName("short").build();
        org.openmrs.Concept conceptSet = new ConceptBuilder().withName("Parent Concept").withClass("Misc").withDataType("N/A").withShortName("Shortn").withUUID("Parent").withSetMember(child1).withSetMember(child2).withSetMember(child3).withSetMember(child4).build();
        List<ConceptRow> conceptRows = conceptSetMapper.mapAll(referenceDataConceptSetMapper.mapAll(conceptSet)).getConceptRows();
        assertEquals(4, conceptRows.size());
        assertEquals("Child1", conceptRows.get(0).name);
        assertEquals("Child2", conceptRows.get(1).name);
        assertEquals("Child3", conceptRows.get(2).name);
        assertEquals("Child4", conceptRows.get(3).name);
        assertEquals("Description", conceptRows.get(0).description);
        assertEquals("Some", conceptRows.get(0).conceptClass);
        assertEquals("N/A", conceptRows.get(0).getDataType());
        assertEquals(0, conceptRows.get(0).getSynonyms().size());
        assertEquals(0, conceptRows.get(1).getSynonyms().size());
        assertEquals(0, conceptRows.get(2).getSynonyms().size());
        assertEquals(0, conceptRows.get(3).getSynonyms().size());
    }

    @Test
    public void getListOfConceptWithConceptAnswers() throws Exception {
        org.openmrs.Concept answer1 = new ConceptBuilder().withName("Answer1").withDataType("N/A").withClass("Misc").withShortName("ShortName3").withUUID("answer1").build();
        org.openmrs.Concept child1 = new ConceptBuilder().withName("Child1").withDataType("N/A").withClass("Misc").withShortName("ShortName1").withUUID("child1").withAnswer(answer1).build();
        org.openmrs.Concept child2 = new ConceptBuilder().withName("Child2").withDataType("N/A").withClass("Misc").withShortName("ShortName2").withUUID("child2").build();
        org.openmrs.Concept conceptSet = new ConceptBuilder().withName("Parent Concept").withClass("Misc").withDataType("N/A").withShortName("Shortn").withUUID("Parent").withSetMember(child1).withSetMember(child2).build();
        ConceptRows conceptRows = conceptSetMapper.mapAll(referenceDataConceptSetMapper.mapAll(conceptSet));
        List<ConceptRow> conceptList = conceptRows.getConceptRows();
        ConceptRow answer = conceptList.get(0);
        ConceptRow child1Row = conceptList.get(1);
        ConceptRow child2Row = conceptList.get(2);
        assertEquals(3, conceptList.size());
        assertEquals("Answer1", answer.name);
        assertEquals("Child1", child1Row.name);
        assertEquals("Child2", child2Row.name);
    }

    @Test
    public void getListOfConceptWithConceptSets() throws Exception {
        org.openmrs.Concept answer1 = new ConceptBuilder().withName("Answer1").withDataType("N/A").withClass("Misc").withShortName("ShortName3").withUUID("answer1").build();
        org.openmrs.Concept child1 = new ConceptBuilder().withName("Child1").withDataType("N/A").withClass("Misc").withShortName("ShortName1").withUUID("child1").withAnswer(answer1).build();
        org.openmrs.Concept child2 = new ConceptBuilder().withName("Child2").withDataType("N/A").withClass("Misc").withShortName("ShortName2").withUUID("child2").build();
        org.openmrs.Concept child3 = new ConceptBuilder().withName("Child3").withDataType("N/A").withClass("Misc").withShortName("ShortName3").withUUID("child3").build();
        org.openmrs.Concept set1 = new ConceptBuilder().withName("Sub Parent").withDataType("N/A").withClass("Misc").withShortName("SubP").withUUID("subp").withSetMember(child3).build();
        set1.setSet(true);
        org.openmrs.Concept conceptSet = new ConceptBuilder().withName("Parent Concept").withClass("Misc").withDataType("N/A").withShortName("Shortn").withUUID("Parent").withSetMember(child1).withSetMember(child2).withSetMember(set1).build();
        conceptSet.setSet(true);
        ConceptRows conceptRows = conceptSetMapper.mapAll(referenceDataConceptSetMapper.mapAll(conceptSet));
        List<ConceptRow> conceptList = conceptRows.getConceptRows();
        List<ConceptSetRow> conceptSetList = conceptRows.getConceptSetRows();
        ConceptRow answer = conceptList.get(0);
        ConceptRow child1Row = conceptList.get(1);
        ConceptRow child2Row = conceptList.get(2);
        assertEquals(4, conceptList.size());
        assertEquals("Answer1", answer.name);
        assertEquals("answer1", answer.uuid);
        assertEquals("Child1", child1Row.name);
        assertEquals("child1", child1Row.uuid);
        assertEquals("child2", child2Row.uuid);
        assertEquals("Child2", child2Row.name);
        assertEquals(2, conceptSetList.size());
        ConceptSetRow conceptSetRow1 = conceptSetList.get(0);
        ConceptSetRow conceptSetRow2 = conceptSetList.get(1);
        assertEquals("Parent Concept", conceptSetRow2.name);
        assertEquals("Parent", conceptSetRow2.uuid);
        assertEquals("Sub Parent", conceptSetRow1.name);
        assertEquals("subp", conceptSetRow1.uuid);
    }

    @Test
    public void uuidNullIfNotSpecified() throws Exception {
        ConceptSetRow conceptRow = new ConceptSetRow();
        conceptRow.uuid = null;
        ConceptSet map = conceptSetMapper.map(conceptRow);
        assertNull(map.getUuid());
    }

    @Test
    public void uuidNullIfNotValid() throws Exception {
        ConceptSetRow conceptSetRow = new ConceptSetRow();
        conceptSetRow.uuid = "invalid UUID";
        ConceptSet map = conceptSetMapper.map(conceptSetRow);
        assertNull(map.getUuid());
    }
}