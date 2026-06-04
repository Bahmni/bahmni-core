package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang3.StringUtils;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.BaseIntegrationTest;
import org.bahmni.module.admin.csv.models.ConceptReferenceTermRow;
import org.bahmni.module.admin.csv.models.FormerConceptReferenceRow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ValidationException;
import org.openmrs.api.context.Context;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

public class ConceptReferenceTermPersisterIT extends BaseIntegrationTest {

    @Autowired
    private ConceptReferenceTermPersister conceptReferenceTermPersister;
    @Autowired
    private ConceptService conceptService;

    private FormerConceptReferenceRow formerConceptReferenceRow;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Context.authenticate("admin", "test");
        conceptReferenceTermPersister.init(Context.getUserContext());
        executeDataSet("conceptSetup.xml");
        executeDataSet("conceptReferenceTerm.xml");
        formerConceptReferenceRow = new FormerConceptReferenceRow();
    }

    @Test
    public void shouldValidateTheGivenFormerReferenceTermRow() {
        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS")));

        assertTrue(conceptReferenceTermPersister.validate(formerConceptReferenceRow).isEmpty());
    }

    @Test
    public void shouldGiveErrorMessageForInvalidConceptName() {
        formerConceptReferenceRow.setConceptName("Not exist");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS")));

        Messages messages = conceptReferenceTermPersister.validate(formerConceptReferenceRow);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        assertEquals("Not exist concept is not present", messages.get(0));
    }

    @Test
    public void shouldGiveErrorMessageForInvalidConceptReferenceSourceName() {
        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.emrapi", "New Code", "SAME-AS")));

        Messages messages = conceptReferenceTermPersister.validate(formerConceptReferenceRow);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        assertEquals("New Code reference term code is not present in org.emrapi source", messages.get(0));
    }

    @Test
    public void shouldGiveErrorMessageForInvalidConceptReferenceTermCode() {
        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "No Code", "SAME-AS")));

        Messages messages = conceptReferenceTermPersister.validate(formerConceptReferenceRow);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        assertEquals("No Code reference term code is not present in org.openmrs.module.emrapi source", messages.get(0));
    }

    @Test
    public void shouldGiveErrorMessageForInvalidReferenceRelationship() {
        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "New Rel")));

        Messages messages = conceptReferenceTermPersister.validate(formerConceptReferenceRow);
        assertFalse(messages.isEmpty());
        assertEquals(1, messages.size());
        assertEquals("New Rel concept map type is not present", messages.get(0));
    }

    @Test
    public void shouldValidateMultipleConceptReferenceTermRow() {
        formerConceptReferenceRow.setConceptName("Not existing Concept");
        List<ConceptReferenceTermRow> referenceTerms = Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS"),
                getConceptReferenceTermRow("org.openmrs.module.emrapi", "No Code", "New Rel"));
        formerConceptReferenceRow.setReferenceTerms(referenceTerms);

        List<String> expectedErrorMessages = Arrays.asList("Not existing Concept concept is not present",
                "No Code reference term code is not present in org.openmrs.module.emrapi source",
                "New Rel concept map type is not present");
        Messages messages = conceptReferenceTermPersister.validate(formerConceptReferenceRow);

        assertFalse(messages.isEmpty());
        assertEquals(3, messages.size());
        assertTrue(messages.containsAll(expectedErrorMessages));
    }

    @Test
    public void shouldPersistNewlyGivenConceptReferenceTermRow() {
        Context.openSession();
        Context.authenticate("admin", "test");

        Concept concept = conceptService.getConceptByName("Existing Concept");
        assertTrue(concept.getConceptMappings().isEmpty());

        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS")));

        conceptReferenceTermPersister.persist(formerConceptReferenceRow);
        Context.flushSession();
        concept = conceptService.getConceptByName("Existing Concept");
        ArrayList<ConceptMap> conceptMappings =  new ArrayList<>(concept.getConceptMappings());

        assertFalse(conceptMappings.isEmpty());
        assertEquals(1, conceptMappings.size());
        assertEquals("SAME-AS", StringUtils.upperCase(conceptMappings.get(0).getConceptMapType().getName()));
        ConceptReferenceTerm conceptReferenceTerm = conceptMappings.get(0).getConceptReferenceTerm();

        assertEquals("New Code", conceptReferenceTerm.getCode());
        assertEquals("org.openmrs.module.emrapi", conceptReferenceTerm.getConceptSource().getName());

        Context.flushSession();
        Context.closeSession();
    }

    @Test
    public void shouldAddNewReferenceButShouldNotRemoveAlreadyExistingConceptReferenceTermsFromConcept() {
        Context.openSession();
        Context.authenticate("admin", "test");

        Concept concept = conceptService.getConceptByName("Existing Concept");
        assertTrue(concept.getConceptMappings().isEmpty());

        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS")));

        conceptReferenceTermPersister.persist(formerConceptReferenceRow);
        Context.flushSession();
        concept = conceptService.getConceptByName("Existing Concept");
        ArrayList<ConceptMap> conceptMappings =  new ArrayList<>(concept.getConceptMappings());

        assertFalse(conceptMappings.isEmpty());
        assertEquals(1, conceptMappings.size());
        ConceptMap conceptMap = conceptMappings.get(0);
        assertEquals("SAME-AS", StringUtils.upperCase(conceptMap.getConceptMapType().getName()));
        ConceptReferenceTerm conceptReferenceTerm = conceptMap.getConceptReferenceTerm();

        assertEquals("New Code", conceptReferenceTerm.getCode());
        assertEquals("org.openmrs.module.emrapi", conceptReferenceTerm.getConceptSource().getName());

        Context.flushSession();
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("IT", "New Code 1", "TEST")));
        conceptReferenceTermPersister.persist(formerConceptReferenceRow);
        Context.flushSession();
        concept = conceptService.getConceptByName("Existing Concept");
        conceptMappings =  new ArrayList<>(concept.getConceptMappings());
        assertEquals(2, conceptMappings.size());
        ConceptMap conceptMap1 = getConceptMapForSourceAndCode(conceptMappings, "org.openmrs.module.emrapi", "New Code");
        assertNotNull("Should Have found Concept Map with the code [New Code]", conceptMap1);
        ConceptMap conceptMap2 = getConceptMapForSourceAndCode(conceptMappings, "IT", "New Code 1");
        assertNotNull("Should Have found Concept Map with the code [New Code 1]", conceptMap1);
        assertEquals("SAME-AS", StringUtils.upperCase(conceptMap1.getConceptMapType().getName()));
        assertEquals("TEST", StringUtils.upperCase(conceptMap2.getConceptMapType().getName()));
        assertEquals(conceptMap, conceptMap1);

        conceptReferenceTerm = conceptMap2.getConceptReferenceTerm();

        assertEquals("New Code 1", conceptReferenceTerm.getCode());
        assertEquals("IT", conceptReferenceTerm.getConceptSource().getName());

        Context.flushSession();
        Context.closeSession();
    }

    private ConceptMap getConceptMapForSourceAndCode(ArrayList<ConceptMap> conceptMappings, String source, String code) {
        return conceptMappings.stream().filter(conceptMap -> conceptMap.getConceptReferenceTerm().getConceptSource().getName().equals(source) && conceptMap.getConceptReferenceTerm().getCode().equals(code)).findFirst().get();
    }

    @Test
    public void shouldNotBeAbleToAddSameReferenceTermTwiceInSameConcept() {
        Context.openSession();
        Context.authenticate("admin", "test");

        formerConceptReferenceRow.setConceptName("Existing Concept");
        formerConceptReferenceRow.setReferenceTerms(Arrays.asList(getConceptReferenceTermRow("org.openmrs.module.emrapi", "New Code", "SAME-AS")));

        conceptReferenceTermPersister.persist(formerConceptReferenceRow);
        Context.flushSession();
        Concept concept = conceptService.getConceptByName("Existing Concept");
        ArrayList<ConceptMap> conceptMappings =  new ArrayList<>(concept.getConceptMappings());

        assertFalse(conceptMappings.isEmpty());
        assertEquals(1, conceptMappings.size());
        ConceptMap conceptMap = conceptMappings.get(0);
        assertEquals("SAME-AS", StringUtils.upperCase(conceptMap.getConceptMapType().getName()));
        ConceptReferenceTerm conceptReferenceTerm = conceptMap.getConceptReferenceTerm();

        assertEquals("New Code", conceptReferenceTerm.getCode());
        assertEquals("org.openmrs.module.emrapi", conceptReferenceTerm.getConceptSource().getName());

        expectedException.expect(ValidationException.class);
        expectedException.expectMessage("ConceptReferenceTerm.term.alreadyMapped");
        conceptReferenceTermPersister.persist(formerConceptReferenceRow);
    }

    private ConceptReferenceTermRow getConceptReferenceTermRow(String source, String code, String relationshipType) {
        return new ConceptReferenceTermRow(source, code, relationshipType);
    }
}