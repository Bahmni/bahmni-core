package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.bahmni.module.fhirterminologyservices.api.TerminologyLookupService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSource;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.fhir2.api.FhirConceptSourceService;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.beans.factory.annotation.Qualifier;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
@PowerMockIgnore("javax.management.*")
public class BahmniDiagnosisAnswerConceptSaveCommandImplTest {

    @Mock
    @Qualifier("adminService")
    AdministrationService administrationService;

    @Mock
    ConceptService conceptService;

    @Mock
    TerminologyLookupService terminologyLookupService;

    @Mock
    private FhirConceptSourceService conceptSourceService;

    @Mock
    private UserContext userContext;

    @InjectMocks
    BahmniDiagnosisAnswerConceptSaveCommandImpl bahmniDiagnosisAnswerConceptSaveCommand;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    final String GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID = "bahmni.diagnosisSetForNewDiagnosisConcepts";

    final String UNCLASSIFIED_CONCEPT_SET_UUID = "unclassified-concept-set-uuid";

    final String MALARIA_CONCEPT_UUID = "malaria-uuid";

    final String MOCK_CONCEPT_SYSTEM = "http://dummyhost.com/systemcode";

    final String MOCK_CONCEPT_SOURCE_CODE = "CS dummy code";

    private final String TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER = "/";

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Context.class);
        Locale defaultLocale = new Locale("en", "GB");
        when(Context.getLocale()).thenReturn(defaultLocale);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldSaveNewDiagnosisAnswerConceptAndAddToUnclassifiedSetWhenConceptSourceAndReferenceCodeProvided() throws Exception {
        Concept newDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount + 1, unclassifiedConceptSet.getSetMembers().size());
        assertEquals(MALARIA_CONCEPT_UUID,bahmniEncounterTransaction.getBahmniDiagnoses().get(0).getCodedAnswer().getUuid());
    }

    @Test
    public void shouldNotCreateDiagnosisAnswerConceptWhenExistingConceptProvided() throws Exception {
        Concept newDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, false);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
    }

    @Test
    public void shouldNotCreateDiagnosisAnswerConceptWhenExistingConceptSourceAndCodeProvided() throws Exception {
        Concept existingDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptService.getConceptByMapping(anyString(), anyString())).thenReturn(existingDiagnosisConcept);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(existingDiagnosisConcept);

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
        assertEquals(MALARIA_CONCEPT_UUID,bahmniEncounterTransaction.getBahmniDiagnoses().get(0).getCodedAnswer().getUuid());
    }

    @Test
    public void shouldThrowExceptionWhenConceptSourceNotFound() throws Exception {
        Concept newDiagnosisConcept = getDiagnosisConcept();
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction("Some Invalid System", true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.empty());
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenReturn(newDiagnosisConcept);

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Concept Source Some Invalid System not found");

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
    }

    @Test
    public void shouldThrowExceptionWhenTerminologyServerUnavailable() throws Exception {
        Concept unclassifiedConceptSet = getUnclassifiedConceptSet();
        BahmniEncounterTransaction bahmniEncounterTransaction = getBahmniEncounterTransaction(MOCK_CONCEPT_SYSTEM, true);
        when(administrationService.getGlobalProperty(GP_DEFAULT_CONCEPT_SET_FOR_DIAGNOSIS_CONCEPT_UUID)).thenReturn(UNCLASSIFIED_CONCEPT_SET_UUID);
        when(conceptSourceService.getConceptSourceByUrl(anyString())).thenReturn(Optional.of(getMockedConceptSources(MOCK_CONCEPT_SYSTEM, MOCK_CONCEPT_SOURCE_CODE)));
        when(conceptService.getConceptByUuid(UNCLASSIFIED_CONCEPT_SET_UUID)).thenReturn(unclassifiedConceptSet);
        when(terminologyLookupService.getConcept(anyString(), anyString())).thenAnswer( invocation -> { throw new Exception("Error fetching concept details from terminology server"); });

        int initialDiagnosisSetMembersCount = unclassifiedConceptSet.getSetMembers().size();

        expectedException.expect(APIException.class);
        expectedException.expectMessage("Exception while getting concept details for concept reference code 61462000");

        bahmniDiagnosisAnswerConceptSaveCommand.update(bahmniEncounterTransaction);

        assertEquals(initialDiagnosisSetMembersCount, unclassifiedConceptSet.getSetMembers().size());
        verify(conceptService, times(0)).saveConcept(any(Concept.class));
    }

    private BahmniEncounterTransaction getBahmniEncounterTransaction(String conceptSystem, boolean isCodedAnswerFromTermimologyServer) {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setBahmniDiagnoses(createBahmniDiagnoses(conceptSystem, isCodedAnswerFromTermimologyServer));
        return bahmniEncounterTransaction;
    }

    private List<BahmniDiagnosisRequest> createBahmniDiagnoses(String conceptSystem, boolean isCodedAnswerFromTermimologyServer) {
        String codedAnswerUuid = null;
        if( isCodedAnswerFromTermimologyServer)
            codedAnswerUuid = conceptSystem + TERMINOLOGY_SERVER_CODED_ANSWER_DELIMITER + "61462000";
        else
            codedAnswerUuid = "coded-answer-uuid";
        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setCertainty(Diagnosis.Certainty.CONFIRMED.name());
        bahmniDiagnosisRequest.setOrder(Diagnosis.Order.PRIMARY.name());
        bahmniDiagnosisRequest.setCodedAnswer(new EncounterTransaction.Concept(codedAnswerUuid));
        bahmniDiagnosisRequest.setDiagnosisStatusConcept(new EncounterTransaction.Concept(null, "Ruled Out"));
        bahmniDiagnosisRequest.setComments("comments");
        bahmniDiagnosisRequest.setEncounterUuid("enc-uuid-1");

        return Arrays.asList(bahmniDiagnosisRequest);
    }

    private ConceptSource getMockedConceptSources(String name, String code) {
        ConceptSource conceptSource = new ConceptSource();
        conceptSource.setName(name);
        conceptSource.setHl7Code(code);
        return conceptSource;
    }

    private Concept getDiagnosisConcept() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Malaria (disorder)", Context.getLocale());
        ConceptName shortName = new ConceptName("Malaria", Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setShortName(shortName);
        concept.setUuid(MALARIA_CONCEPT_UUID);

        return concept;
    }

    private Concept getUnclassifiedConceptSet() {
        Concept concept = new Concept();
        ConceptName fullySpecifiedName = new ConceptName("Unclassified", Context.getLocale());

        concept.setFullySpecifiedName(fullySpecifiedName);
        concept.setSet(true);
        return concept;
    }
}
