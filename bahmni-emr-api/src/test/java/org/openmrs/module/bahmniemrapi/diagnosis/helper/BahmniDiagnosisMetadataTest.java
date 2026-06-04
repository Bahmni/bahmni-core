package org.openmrs.module.bahmniemrapi.diagnosis.helper;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.builder.ConceptBuilder;
import org.openmrs.module.bahmniemrapi.builder.ObsBuilder;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosis;
import org.openmrs.module.bahmniemrapi.diagnosis.contract.BahmniDiagnosisRequest;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.EncounterTransactionMapper;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.util.LocaleUtility;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@PrepareForTest({Context.class,LocaleUtility.class})
@RunWith(PowerMockRunner.class)
public class BahmniDiagnosisMetadataTest {
    @Mock
    private ObsService obsService;
    @Mock
    private ConceptService conceptService;
    @Mock
    private EncounterTransactionMapper eTMapper;

    @Mock(answer= Answers.RETURNS_DEEP_STUBS)
    private EmrApiProperties properties;
    public static final String BOOLEAN_UUID = "8d4a5cca-c2cc-11de-8d13-0010c6dffd0f";
    private static final String BAHMNI_DIAGNOSIS_STATUS = "Bahmni Diagnosis Status";
    private static final String BAHMNI_DIAGNOSIS_REVISED = "Bahmni Diagnosis Revised";
    private static final String BAHMNI_INITIAL_DIAGNOSIS = "Bahmni Initial Diagnosis";

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mockStatic(LocaleUtility.class);
        mockStatic(Context.class);
        PowerMockito.when(Context.getLocale()).thenReturn(Locale.ENGLISH);
        PowerMockito.when(LocaleUtility.getDefaultLocale()).thenReturn(Locale.ENGLISH);
        PowerMockito.when(Context.getConceptService()).thenReturn(conceptService);

    }


    @Test
    public void shouldMatchCodedDiagnosis() {
        Concept diagnosisSetConcept = conceptForName("Diagnosis Concept Set");
        Concept codedDiagnosisConcept = conceptForName("Coded Diagnosis");
        when(properties.getDiagnosisMetadata().getDiagnosisSetConcept()).thenReturn(diagnosisSetConcept);
        when(properties.getDiagnosisMetadata().getCodedDiagnosisConcept()).thenReturn(codedDiagnosisConcept);
        Concept feverConcept = conceptForName("Fever");
        Obs diagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(feverConcept).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();
        Obs anotherDiagnosis =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(conceptForName("Another diagnosis")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();

        Obs randomObs = new ObsBuilder().withConcept(conceptForName("Random Concept")).withValue("Hello World").build();

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(new EncounterTransaction.Concept(feverConcept.getUuid(), feverConcept.getName
                ().getName(), false));

        BahmniDiagnosisMetadata bahmniDiagnosisMetadata = new BahmniDiagnosisMetadata(obsService, conceptService, properties, null);
        Obs matchingDiagnosis = bahmniDiagnosisMetadata.findMatchingDiagnosis(Arrays.asList(diagnosisObs,
                anotherDiagnosis,
                randomObs), bahmniDiagnosis);
        assertThat(matchingDiagnosis, is(sameInstance(diagnosisObs)));

    }

    @Test
    public void shouldMatchNonCodedDiagnosis() {
        Concept diagnosisSetConcept = conceptForName("Diagnosis Concept Set");
        Concept codedDiagnosisConcept = conceptForName("Coded Diagnosis");
        Concept nonCodedDiagnosisConcept = conceptForName("Non Coded Diagnosis");
        when(properties.getDiagnosisMetadata().getDiagnosisSetConcept()).thenReturn(diagnosisSetConcept);
        when(properties.getDiagnosisMetadata().getCodedDiagnosisConcept()).thenReturn(codedDiagnosisConcept);
        when(properties.getDiagnosisMetadata().getNonCodedDiagnosisConcept()).thenReturn(nonCodedDiagnosisConcept);
        Obs codedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                 new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(conceptForName("Fever")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();
        Obs nonCodedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                new ObsBuilder().withConcept(nonCodedDiagnosisConcept)
                                        .withValue("Free text diagnosis").build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();

        Obs randomObs = new ObsBuilder().withConcept(conceptForName("Random Concept")).withValue("Hello World").build();

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setFreeTextAnswer("Free text diagnosis");

        BahmniDiagnosisMetadata bahmniDiagnosisMetadata = new BahmniDiagnosisMetadata(obsService, conceptService, properties, null);
        Obs matchingDiagnosis = bahmniDiagnosisMetadata.findMatchingDiagnosis(Arrays.asList(codedDiagnosisObs,
                nonCodedDiagnosisObs,
                randomObs), bahmniDiagnosis);
        assertThat(matchingDiagnosis, is(sameInstance(nonCodedDiagnosisObs)));

    }

    @Rule
    public ExpectedException thrown= ExpectedException.none();

    @Test
    public void shouldThrowExceptionWhenMoreThanOneMatchingDiagnosisFound() {
        Concept diagnosisSetConcept = conceptForName("Diagnosis Concept Set");
        Concept codedDiagnosisConcept = conceptForName("Coded Diagnosis");
        Concept nonCodedDiagnosisConcept = conceptForName("Non Coded Diagnosis");
        when(properties.getDiagnosisMetadata().getDiagnosisSetConcept()).thenReturn(diagnosisSetConcept);
        when(properties.getDiagnosisMetadata().getCodedDiagnosisConcept()).thenReturn(codedDiagnosisConcept);
        when(properties.getDiagnosisMetadata().getNonCodedDiagnosisConcept()).thenReturn(nonCodedDiagnosisConcept);
        Concept feverConcept = conceptForName("Fever");
        Obs aCodedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                 new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(feverConcept).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();
        Obs anotherCodedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                 new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(feverConcept).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();
        Obs randomObs = new ObsBuilder().withConcept(conceptForName("Random Concept")).withValue("Hello World").build();

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(new EncounterTransaction.Concept(feverConcept.getUuid(), feverConcept.getName
                ().getName(), false));

        BahmniDiagnosisMetadata bahmniDiagnosisMetadata = new BahmniDiagnosisMetadata(obsService, conceptService, properties, null);

        thrown.expect(RuntimeException.class);
        thrown.expectMessage("The same diagnosis cannot be saved more than once");
        bahmniDiagnosisMetadata.findMatchingDiagnosis(
                Arrays.asList(aCodedDiagnosisObs, anotherCodedDiagnosisObs, randomObs), bahmniDiagnosis);
    }

    @Test
    public void shouldNotConsiderRevisedObsWhileFindingMatchingObs() throws Exception {
        Concept diagnosisSetConcept = conceptForName("Diagnosis Concept Set");
        Concept codedDiagnosisConcept = conceptForName("Coded Diagnosis");
        Concept nonCodedDiagnosisConcept = conceptForName("Non Coded Diagnosis");
        Concept conceptTrue = conceptForName("TRUE");
        Concept revised = conceptForName("Revised");
        when(properties.getDiagnosisMetadata().getDiagnosisSetConcept()).thenReturn(diagnosisSetConcept);
        when(properties.getDiagnosisMetadata().getCodedDiagnosisConcept()).thenReturn(codedDiagnosisConcept);
        when(properties.getDiagnosisMetadata().getNonCodedDiagnosisConcept()).thenReturn(nonCodedDiagnosisConcept);
        when(conceptService.getTrueConcept()).thenReturn(conceptTrue);
        when(conceptService.getConceptsByName(BAHMNI_DIAGNOSIS_REVISED,Locale.ENGLISH,false)).thenReturn(Arrays.asList(revised));
        Concept feverConcept = conceptForName("Fever");
        Obs aCodedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(feverConcept).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build(),
                                new ObsBuilder().withConcept(revised)
                                        .withValue(conceptTrue).build()).build();
        Obs anotherCodedDiagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(
                                new ObsBuilder().withConcept(codedDiagnosisConcept)
                                        .withValue(feverConcept).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build()
                        ).build();
        Obs randomObs = new ObsBuilder().withConcept(conceptForName("Random Concept")).withValue("Hello World").build();

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(new EncounterTransaction.Concept(feverConcept.getUuid(), feverConcept.getName
                ().getName(), false));

        BahmniDiagnosisMetadata bahmniDiagnosisMetadata = new BahmniDiagnosisMetadata(obsService, conceptService, properties, null);
        Obs matchingDiagnosis = bahmniDiagnosisMetadata.findMatchingDiagnosis(
                Arrays.asList(aCodedDiagnosisObs, anotherCodedDiagnosisObs, randomObs), bahmniDiagnosis);
        assertNotNull(matchingDiagnosis);
    }

    @Test
    public void shouldUpdateInitialDiagnosisAsCurrentDiagnosisWhenInitialDiagnosisIsVoided() throws Exception {
        Concept initialDiagnosisConcept = conceptForName(BAHMNI_INITIAL_DIAGNOSIS);
        String initialDiagnosisUuid = "initial-obs-uuid";
        String existingObsUuid = "existing-obs-uuid";
        Obs initialDiagnosisObs = new ObsBuilder().
                withConcept(conceptForName("Dehydration")).withVoided().build();
        Concept diagnosisSetConcept = conceptForName("Diagnosis Concept Set");
        Concept revised = conceptForName(BAHMNI_DIAGNOSIS_REVISED);
        Concept falseConcept = conceptForName("FALSE");
        when(conceptService.getConceptByName(BAHMNI_DIAGNOSIS_REVISED)).thenReturn(revised);
        when(conceptService.getFalseConcept()).thenReturn(falseConcept);
        when(conceptService.getConceptByName(BAHMNI_INITIAL_DIAGNOSIS)).thenReturn(initialDiagnosisConcept);
        when(obsService.getObsByUuid(initialDiagnosisUuid)).thenReturn(initialDiagnosisObs);
        EncounterTransaction encounterTransaction = new EncounterTransaction();
        encounterTransaction.addDiagnosis(new EncounterTransaction.Diagnosis().setExistingObs(existingObsUuid));
        when(eTMapper.map(Matchers.any(Encounter.class),eq(false))).thenReturn(encounterTransaction);
        Obs diagnosisObs =
                new ObsBuilder().withConcept(diagnosisSetConcept)
                        .withGroupMembers(

                                new ObsBuilder().withConcept(conceptForName("Diagnosis Order"))
                                        .withValue(conceptForName("Primary")).build(),
                                new ObsBuilder().withConcept(conceptForName("Diagnosis Certainty"))
                                        .withValue(conceptForName("Confirmed")).build(),
                                new ObsBuilder().withConcept(initialDiagnosisConcept)
                                        .withValue(initialDiagnosisUuid).build(),
                                new ObsBuilder().withConcept(revised)
                                        .withValue(falseConcept).build())
                        .withEncounter(new Encounter())
                        .withCreator(createUser("Ram")).build();
        diagnosisObs.setUuid(existingObsUuid);
        when(obsService.getObsByUuid(existingObsUuid)).thenReturn(diagnosisObs);

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setExistingObs(existingObsUuid);
        BahmniDiagnosisMetadata bahmniDiagnosisMetadata = new BahmniDiagnosisMetadata(obsService, conceptService, properties, eTMapper);
        BahmniDiagnosisRequest bahmniDiagnosisRequest = bahmniDiagnosisMetadata.mapBahmniDiagnosis(bahmniDiagnosis, null, true, false, true, true);

        ArgumentCaptor<Obs> obsArgumentCaptor = ArgumentCaptor.forClass(Obs.class);
        verify(obsService).saveObs(obsArgumentCaptor.capture(), eq("Initial obs got voided"));
        Obs obs = obsArgumentCaptor.getValue();
        assertThat(obs.getValueText(),is(existingObsUuid));
        assertThat(bahmniDiagnosisRequest.getFirstDiagnosis().getExistingObs(),is(existingObsUuid));
    }

    public Concept conceptForName(String conceptName) {
        return new ConceptBuilder().withName(conceptName).build();
    }

    private User createUser(String name){
        Person person = new Person();
        person.addName(new PersonName(name,null,null));
        return new User(person);
    }
}
