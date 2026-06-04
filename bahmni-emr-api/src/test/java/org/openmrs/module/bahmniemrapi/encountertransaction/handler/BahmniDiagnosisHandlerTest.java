package org.openmrs.module.bahmniemrapi.encountertransaction.handler;

import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.builder.ConceptBuilder;
import org.openmrs.module.bahmniemrapi.builder.EncounterBuilder;
import org.openmrs.module.bahmniemrapi.builder.ObsBuilder;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.bahmniemrapi.diagnosis.helper.BahmniDiagnosisMetadata;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Set;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class})
@RunWith(PowerMockRunner.class)

public class BahmniDiagnosisHandlerTest {

    public static final String BOOLEAN_DATATYPE_UUID = "8d4a5cca-c2cc-11de-8d13-0010c6dffd0f";
    @Mock
    private BahmniDiagnosisMetadata bahmniDiagnosisMetadata;
    private Concept statusConcept = new Concept();
    private Concept revisedConcept = new Concept();
    private Concept initialDiagnosisConcept = new Concept();
    @Mock
    private ConceptService conceptService;
    @Mock
    private EmrApiProperties emrApiProperties;
    @Mock
    private ObsService obsService;

    @Before
    public void setup() {
        initMocks(this);
        mockStatic(Context.class);
        ConceptDatatype booleanDataType = new ConceptDatatype();
        booleanDataType.setUuid(BOOLEAN_DATATYPE_UUID);
        revisedConcept.setDatatype(booleanDataType);
        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);
        Concept trueConcept = new Concept();
        when(conceptService.getTrueConcept()).thenReturn(trueConcept);
        when(conceptService.getFalseConcept()).thenReturn(new Concept());

        statusConcept.setUuid(String.valueOf(UUID.randomUUID()));
        revisedConcept.setUuid(String.valueOf(UUID.randomUUID()));
        initialDiagnosisConcept.setUuid(String.valueOf(UUID.randomUUID()));

        when(bahmniDiagnosisMetadata.getBahmniDiagnosisStatusConcept()).thenReturn(statusConcept);
        when(bahmniDiagnosisMetadata.getBahmniDiagnosisRevisedConcept()).thenReturn(revisedConcept);
        when(bahmniDiagnosisMetadata.getBahmniInitialDiagnosisConcept()).thenReturn(initialDiagnosisConcept);
    }

    @Test
    public void shouldSaveStatusWhenFirstSave() {
        EncounterTransaction encounterTransaction = new EncounterTransaction();


        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setCertainty("CONFIRMED");
        EncounterTransaction.Concept codedAnswer = new EncounterTransaction.Concept();
        codedAnswer.setName("Fever");
        EncounterTransaction.Concept etStatusConcept = new EncounterTransaction.Concept();
        String RULED_OUT_CONCEPT = "Ruled out";
        etStatusConcept.setName(RULED_OUT_CONCEPT);
        bahmniDiagnosisRequest.setCodedAnswer(codedAnswer);
        bahmniDiagnosisRequest.setDiagnosisStatusConcept(etStatusConcept);
        encounterTransaction.addDiagnosis(bahmniDiagnosisRequest);

        Encounter encounter = new EncounterBuilder().build();
        Obs diagnosisObs = new ObsBuilder()
                .withConcept(new ConceptBuilder().withName("Diagnosis Concept Set").build())
                .withGroupMembers(new Obs[]{})
                .build();
        encounter.addObs(diagnosisObs);

        when(bahmniDiagnosisMetadata.findMatchingDiagnosis(encounter.getObs(), bahmniDiagnosisRequest)).thenReturn
                (diagnosisObs);

        when(bahmniDiagnosisMetadata.diagnosisSchemaContainsStatus()).thenReturn(true);
        Concept ruledOutConcept = new Concept();
        when(conceptService.getConcept(RULED_OUT_CONCEPT)).thenReturn(ruledOutConcept);



        new BahmniDiagnosisHandler(bahmniDiagnosisMetadata, obsService, conceptService).forSave(encounter,
                encounterTransaction);

        Set<Obs> groupMembers = diagnosisObs.getGroupMembers();
        assertEquals(3, groupMembers.size());
        assertThat(groupMembers, hasItem(containsObsWith(statusConcept, ruledOutConcept)));
        assertThat(groupMembers, hasItem(containsObsWith(revisedConcept, conceptService.getFalseConcept())));
        assertThat(groupMembers, hasItem(containsObsWith(initialDiagnosisConcept, diagnosisObs.getUuid())));
    }

    @Test
    public void shouldUpdateValueOfRevisedInPreviousDiagnosisWithTrue() {
        EncounterTransaction encounterTransaction = new EncounterTransaction();


        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        bahmniDiagnosisRequest.setCertainty("CONFIRMED");
        EncounterTransaction.Concept codedAnswer = new EncounterTransaction.Concept();
        codedAnswer.setName("Fever");
        EncounterTransaction.Concept etStatusConcept = new EncounterTransaction.Concept();
        String RULED_OUT_CONCEPT = "Ruled out";
        etStatusConcept.setName(RULED_OUT_CONCEPT);
        bahmniDiagnosisRequest.setCodedAnswer(codedAnswer);
        bahmniDiagnosisRequest.setDiagnosisStatusConcept(etStatusConcept);
        bahmniDiagnosisRequest.setPreviousObs(String.valueOf(UUID.randomUUID()));
        BahmniDiagnosis firstDiagnosis = new BahmniDiagnosis();
        firstDiagnosis.setExistingObs("i dont care");
        bahmniDiagnosisRequest.setFirstDiagnosis(firstDiagnosis);

        encounterTransaction.addDiagnosis(bahmniDiagnosisRequest);

        Encounter encounter = new EncounterBuilder().build();
        Obs diagnosisObs = new ObsBuilder()
                .withConcept(new ConceptBuilder().withName("Diagnosis Concept Set").build())
                .withGroupMembers(new Obs[]{})
                .build();
        encounter.addObs(diagnosisObs);

        when(bahmniDiagnosisMetadata.findMatchingDiagnosis(encounter.getObs(), bahmniDiagnosisRequest)).thenReturn
                (diagnosisObs);
        when(bahmniDiagnosisMetadata.diagnosisSchemaContainsStatus()).thenReturn(true);
        Concept ruledOutConcept = new Concept();
        when(conceptService.getConcept(RULED_OUT_CONCEPT)).thenReturn(ruledOutConcept);
        Obs nonRevisedConcept = new ObsBuilder()
                .withConcept(revisedConcept)
                .withValue(conceptService.getFalseConcept()).build();
        Obs previousObs = new ObsBuilder().withGroupMembers(
                nonRevisedConcept
        ).build();
        when(obsService.getObsByUuid(bahmniDiagnosisRequest.getPreviousObs())).thenReturn(previousObs);


        new BahmniDiagnosisHandler(bahmniDiagnosisMetadata, obsService, conceptService).forSave(encounter,
                encounterTransaction);

        Set<Obs> groupMembers = diagnosisObs.getGroupMembers();
        assertEquals(3, groupMembers.size());
        assertThat(groupMembers, hasItem(containsObsWith(statusConcept, ruledOutConcept)));
        assertThat(groupMembers, hasItem(containsObsWith(revisedConcept, conceptService.getFalseConcept())));
        assertThat(groupMembers, hasItem(containsObsWith(initialDiagnosisConcept,
                bahmniDiagnosisRequest.getFirstDiagnosis().getExistingObs())));
        assertThat(nonRevisedConcept.getValueCoded(), is(equalTo(conceptService.getTrueConcept())));
        verify(obsService).saveObs(previousObs, "Diagnosis is revised");
    }

    @Test
    public void shouldHaveTheSameInitialDiagnosisAcrossMultipleSave() {
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        String existingObsUUid = "ExistingObsUUID";

        BahmniDiagnosisRequest bahmniDiagnosisRequest = new BahmniDiagnosisRequest();
        BahmniDiagnosis firstDiagnosis = new BahmniDiagnosis();
        firstDiagnosis.setExistingObs(existingObsUUid);
        bahmniDiagnosisRequest.setFirstDiagnosis(firstDiagnosis);
        encounterTransaction.addDiagnosis(bahmniDiagnosisRequest);
        Encounter encounter = new EncounterBuilder().build();
        Obs initialObs =new ObsBuilder().withConcept(initialDiagnosisConcept).withGroupMembers(new Obs[]{}).build();
        initialObs.setId(123);
        Obs diagnosisObs = new ObsBuilder()
                .withConcept(new ConceptBuilder().withName("Diagnosis Concept Set").build())
                .withGroupMembers(initialObs)
                .build();
        encounter.addObs(diagnosisObs);

        when(bahmniDiagnosisMetadata.findMatchingDiagnosis(encounter.getObsAtTopLevel(false), bahmniDiagnosisRequest)).thenReturn
                (diagnosisObs);

        new BahmniDiagnosisHandler(bahmniDiagnosisMetadata, obsService, conceptService).forSave(encounter,
                encounterTransaction);

        Set<Obs> groupMembers = diagnosisObs.getGroupMembers();
        assertEquals(2, groupMembers.size());
        assertThat(groupMembers, hasItem(containsObsWith(revisedConcept, conceptService.getFalseConcept())));
        assertThat(groupMembers, hasItem(containsObsWith(initialDiagnosisConcept, existingObsUUid)));
    }

    private Matcher<Iterable<? extends Obs>> containsObsWith(Concept concept, Concept value) {
        return Matchers.allOf(hasProperty("concept", is(equalTo(concept)))
                , hasProperty("valueCoded", is(equalTo(value))));
    }

    private Matcher<Iterable<? extends Obs>> containsObsWith(Concept concept, String value) {
        return Matchers.allOf(hasProperty("concept", is(equalTo(concept)))
                , hasProperty("valueText", is(equalTo(value))));
    }

}
