package org.bahmni.module.bahmnicore.dao.impl;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.dao.BahmniConceptDao;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Drug;
import org.openmrs.api.ConceptService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class BahmniConceptDaoImplIT extends BaseIntegrationTest{
    @Autowired
    private BahmniConceptDao bahmniConceptDao;

    @Autowired
    private ConceptService conceptService;
    private Concept questionConcept;

    @Test
    public void shouldReturnNonVoidedAnswersForAQuestion() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, "Aneurism");

        assertThat(result.size(), is(equalTo(1)));

        Concept resultConcept = result.iterator().next().getAnswerConcept();
        assertTrue(resultConcept.getId().equals(902));
    }

    @Test
    public void shouldIgnoreCaseWhenSearching() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, "aNeUrIsM");

        assertThat(result.size(), is(equalTo(1)));

        Concept resultConcept = result.iterator().next().getAnswerConcept();
        assertTrue(resultConcept.getId().equals(902));
    }

    @Test
    public void shouldNotReturnVoidedAnswers() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);
        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, "Porphyria");
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void shouldSearchEachTermByQuestion() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        //Searching for "Abscess, Skin"
        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, " ab sk  ");
        assertThat(result.size(), is(equalTo(1)));
        Concept resultConcept = result.iterator().next().getAnswerConcept();
        assertTrue(resultConcept.getId().equals(903));

        result = bahmniConceptDao.searchByQuestion(questionConcept, "in  ab");
        assertThat(result.size(), is(equalTo(2)));

        ArrayList<ConceptAnswer> actualConceptAnswers = new ArrayList<>(result);
        ArrayList<Concept> actualConcepts = new ArrayList<>();
        actualConcepts.add(actualConceptAnswers.get(0).getAnswerConcept());
        actualConcepts.add(actualConceptAnswers.get(1).getAnswerConcept());

        assertThat(actualConcepts, containsInAnyOrder(conceptService.getConcept(902), conceptService.getConcept(903)));

        result = bahmniConceptDao.searchByQuestion(questionConcept, "in  and another term that is not present");
        assertThat(result.size(), is(equalTo(0)));
    }

    @Test
    public void shouldReturnMultipleResultsIfAvailable() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, "ab");

        assertThat(result.size(), is(equalTo(2)));

        ArrayList<ConceptAnswer> actualConceptAnswers = new ArrayList<>(result);
        ArrayList<Concept> actualConcepts = new ArrayList<>();
        actualConcepts.add(actualConceptAnswers.get(0).getAnswerConcept());
        actualConcepts.add(actualConceptAnswers.get(1).getAnswerConcept());

        assertThat(actualConcepts, containsInAnyOrder(conceptService.getConcept(902), conceptService.getConcept(903)));
    }

    @Test
    public void getConceptByFullySpecifiedNameShouldGetConceptByItsFullySpecifiedName() throws Exception{
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Concept result = bahmniConceptDao.getConceptByFullySpecifiedName("Acne");

        assertNotNull(result);
        assertEquals("65230431-2fe5-49fc-b535-ae42bc90979d",result.getUuid());
    }

    @Test
    public void getConceptByFullySpecifiedNameShouldBeCaseInsensitive() throws Exception{
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Concept result = bahmniConceptDao.getConceptByFullySpecifiedName("ACNE");

        assertNotNull(result);
        assertEquals("65230431-2fe5-49fc-b535-ae42bc90979d",result.getUuid());
    }

    @Test
    public void searchByQuestionShouldGetAllConceptAnswersWhenQueryIsEmpty() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        questionConcept = conceptService.getConcept(90);

        Collection<ConceptAnswer> result = bahmniConceptDao.searchByQuestion(questionConcept, null);

        assertEquals(4,result.size());
    }

    @Test
    public void getByConceptSetShouldRetrieveDrugsForSetMembersOfTheConceptSet() throws Exception {
        executeDataSet("drugsWithConcepts.xml");

        Collection<Drug> drugs = bahmniConceptDao.getDrugByListOfConcepts(
                conceptService.getConceptsByConceptSet(conceptService.getConcept(3010)));
        assertEquals(3, drugs.size());
        assertThat(drugs, containsInAnyOrder(conceptService.getDrug(2001), conceptService.getDrug(4001), conceptService.getDrug(6001)));
    }

    @Test
    public void shouldSearchDrugsByDrugNameInTheGivenListOfConcepts() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> resultantDrugs = bahmniConceptDao.searchDrugsByDrugName(3010, "Isoniazid");

        assertEquals(1,resultantDrugs.size());
        assertEquals(conceptService.getDrug(4001),resultantDrugs.get(0));

    }

    @Test
    public void shouldSearchDrugsByDrugNameInTheGivenListOfConceptsIrrespectiveOfCase() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> resultantDrugs = bahmniConceptDao.searchDrugsByDrugName(3010, "IsOnIazId");

        assertEquals(1,resultantDrugs.size());
        assertEquals(conceptService.getDrug(4001),resultantDrugs.get(0));

    }

    @Test
    public void shouldGetAllDrugsInTheGivenListOfConceptsWhichMatchTheDrugConceptNameAsWell() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> drugs = bahmniConceptDao.searchDrugsByDrugName(3010, "t");

        assertEquals(3,drugs.size());
        assertThat(drugs, containsInAnyOrder(conceptService.getDrug(2001), conceptService.getDrug(4001), conceptService.getDrug(6001)));
    }

    @Test
    public void shouldGetAllDrugsInTheGivenListOfConceptsWhenSearchTermNotGiven() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> drugs = bahmniConceptDao.searchDrugsByDrugName(3010, null);

        assertEquals(3,drugs.size());
        assertThat(drugs, containsInAnyOrder(conceptService.getDrug(2001), conceptService.getDrug(4001), conceptService.getDrug(6001)));
    }

    @Test
    public void shouldGetMatchingDrugsInSortedOrder_wrt_sortWeightWhenSearchTermIsGiven() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> drugs = bahmniConceptDao.searchDrugsByDrugName(3010, "t");

        assertEquals(3,drugs.size());

        assertEquals(conceptService.getDrug(2001),drugs.get(0));
        assertEquals(conceptService.getDrug(4001),drugs.get(1));
        assertEquals(conceptService.getDrug(6001),drugs.get(2));
    }

    @Test
    public void shouldGetMatchingDrugsInSortedOrder_wrt_sortWeightWhenSearchTermIsNotGiven() throws Exception{
        executeDataSet("drugsWithConcepts.xml");

        List<Drug> drugs = bahmniConceptDao.searchDrugsByDrugName(3010, null);

        assertEquals(3,drugs.size());

        assertEquals(conceptService.getDrug(2001),drugs.get(0));
        assertEquals(conceptService.getDrug(4001),drugs.get(1));
        assertEquals(conceptService.getDrug(6001),drugs.get(2));
    }

    @Test
    public void shouldGetConceptsByFullySpecifiedName() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("List of Diagnoses");
        conceptNames.add("Dengue Fever");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(2, concepts.size());
        assertEquals(90,concepts.get(0).getConceptId().intValue());
        assertEquals(901,concepts.get(1).getConceptId().intValue());
    }

    @Test
    public void shouldReturnEmptyConceptsListIfConceptNamesNotExist() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("concept1");
        conceptNames.add("concept2");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(0, concepts.size());
    }

    @Test
    public void shouldReturnConceptsOnlyByFullySpecifiedName() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("acne");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(1, concepts.size());
    }

    @Test
    public void shouldGetConceptsByUsingConceptNamesBasedOnCaseInsensitivity() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("List Of diagnoses");
        conceptNames.add("Dengue fever");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(2, concepts.size());
    }

    @Test
    public void shouldNotGetTheConceptsByShortName() throws Exception {
        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("Skin");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(0, concepts.size());
    }

    @Test
    public void shouldNotReturnConceptIfConceptNameIsVoided() throws Exception {

        executeDataSet("sampleCodedConcept.xml");
        List<String> conceptNames = new ArrayList<>();
        conceptNames.add("Acute Porphyria (voided)");

        List<Concept> concepts = bahmniConceptDao.getConceptsByFullySpecifiedName(conceptNames);

        assertEquals(0, concepts.size());
    }
}