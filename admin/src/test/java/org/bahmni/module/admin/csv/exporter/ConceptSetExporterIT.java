package org.bahmni.module.admin.csv.exporter;

import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.admin.csv.models.ConceptRows;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.api.APIException;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ConceptSetExporterIT extends BaseIntegrationTest {

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private ConceptSetExporter conceptSetExporter;

    @Before
    public void setUp() throws Exception {
        executeDataSet("conceptExportSetup.xml");
    }

    @Test
    public void throwExceptionIfConceptDoesNotExist() throws Exception {
        exception.expect(APIException.class);
        exception.expectMessage("Concept Does not exist not found");
        conceptSetExporter.exportConcepts("Does not exist");
    }

    @Test
    @Ignore
    public void getListOfConceptRows() throws Exception {
        ConceptRows result = conceptSetExporter.exportConcepts("Big Concept");
        List<ConceptRow> conceptRows = result.getConceptRows();
        List<ConceptSetRow> conceptSetRows = result.getConceptSetRows();
        assertEquals(9, conceptRows.size());
        ConceptRow child1 = conceptRows.get(4);
        ConceptRow child2 = conceptRows.get(6);
        ConceptRow child3 = conceptRows.get(8);
        ConceptRow child4 = conceptRows.get(7);
        ConceptRow answer01 = conceptRows.get(1);
        ConceptRow answer11 = conceptRows.get(2);
        ConceptRow answer12 = conceptRows.get(3);
        ConceptRow answer21 = conceptRows.get(5);
        assertEquals("Answer1", answer11.name);
        assertEquals("Answer0", answer01.name);
        assertEquals("Answer2", answer12.name);
        assertEquals("Answer3", answer21.name);
        assertEquals("Child1", child1.name);
        assertEquals("d670df13-7fef-44af-aade-8db46f245522", child1.uuid);
        assertEquals("Child2", child2.name);
        assertEquals("Child3", child3.name);
        assertEquals("Child4", child4.name);
        assertEquals("Document", child1.dataType);
        assertEquals("Document", child2.dataType);
        assertEquals("Document", child3.dataType);
        assertEquals("New Class", child1.conceptClass);
        assertEquals("New Class", child2.conceptClass);
        assertEquals("New Class", child3.conceptClass);
        assertEquals(2, child1.getSynonyms().size());
        assertEquals(2, child2.getSynonyms().size());
        assertEquals(2, child3.getSynonyms().size());
        assertEquals(3, child1.getAnswers().size());
        assertEquals(3, child2.getAnswers().size());
        assertEquals(3, child3.getAnswers().size());
        assertEquals("Concept1 Description", child1.getDescription());
        assertNull(child2.getDescription());
        assertNull(child3.getDescription());
        assertEquals("New Code", child3.getReferenceTerms().get(0).getReferenceTermCode());
        assertEquals("SAME-AS".toLowerCase(), child3.getReferenceTerms().get(0).getReferenceTermRelationship().toLowerCase());
        assertEquals("org.openmrs.module.emrapi", child3.getReferenceTerms().get(0).getReferenceTermSource());
        assertEquals(3, conceptSetRows.size());
        ConceptSetRow small = conceptSetRows.get(1);
        ConceptSetRow big = conceptSetRows.get(2);
        assertEquals("Small Concept", small.name);
        assertEquals("68637e4e-c8a9-4831-93b4-2ef2d987105d", small.uuid);
        assertEquals("Big Concept", big.name);
        assertEquals("39854ddf-b950-4c20-91d9-475729ca0ec6", big.uuid);
        assertEquals(3, conceptRows.get(0).getAnswers().size());
        assertEquals(2, conceptRows.get(0).getSynonyms().size());
        assertEquals(5, conceptSetRows.get(0).getChildren().size());
    }
}