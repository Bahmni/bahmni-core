package org.bahmni.module.bahmnicore.forms2.mapper;

import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.util.FormUtil;
import org.bahmni.module.bahmnicore.model.Provider;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.verification.VerificationModeFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.Visit;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

@PrepareForTest({FormUtil.class})
@RunWith(PowerMockRunner.class)
public class FormDetailsMapperTest {

    private String formFieldPath = "formName.2/1-0";
    private String encounterUuid = "encounter-Uuid";
    private String visitUuid = "visitUuid";
    private String providerName = "Super Man";
    private String providerUuid = "provider-uuid";
    private String formName = "formName";
    private int formVersion = 2;
    private Date encounterDateTime = new Date();
    private Date visitStartDateTime = new Date();

    private Obs obs = mock(Obs.class);
    private Encounter encounter = mock(Encounter.class);
    private Visit visit = mock(Visit.class);
    private User anotherCreator = mock(User.class);
    private PersonName personName = mock(PersonName.class);

    @Before
    public void setUp() {

        //mockStatic(FormUtil.class);

        when(obs.getEncounter()).thenReturn(encounter);
        when(obs.getCreator()).thenReturn(anotherCreator);
        when(obs.getFormFieldPath()).thenReturn(formFieldPath);
        when(encounter.getVisit()).thenReturn(visit);
//        when(FormUtil.getFormNameFromFieldPath(formFieldPath)).thenReturn(formName);
//        when(FormUtil.getFormVersionFromFieldPath(formFieldPath)).thenReturn(formVersion);

        when(encounter.getUuid()).thenReturn(encounterUuid);
        when(encounter.getEncounterDatetime()).thenReturn(encounterDateTime);

        when(visit.getUuid()).thenReturn(visitUuid);
        when(visit.getStartDatetime()).thenReturn(visitStartDateTime);

        when(anotherCreator.getPersonName()).thenReturn(personName);
        when(personName.getFullName()).thenReturn(providerName);
        when(anotherCreator.getUuid()).thenReturn(providerUuid);
    }

    @Test
    public void shouldReturnFormDetailsFromGivenObsAndFormTypeOfFormBuilder() {

        Collection<FormDetails> formDetailsCollection = FormDetailsMapper
                .createFormDetails(singletonList(obs), FormType.FORMS2);

        assertEquals(1, formDetailsCollection.size());

        FormDetails formDetails = formDetailsCollection.iterator().next();
        assertEquals("v2", formDetails.getFormType());
        assertEquals(formName, formDetails.getFormName());
        assertEquals(formVersion, formDetails.getFormVersion());

        verifyCommonData(formDetails);

        verify(obs, times(2)).getFormFieldPath();
        //verifyStatic(VerificationModeFactory.times(1));
        assertEquals(formName, FormUtil.getFormNameFromFieldPath(formFieldPath));
        //verifyStatic(VerificationModeFactory.times(1));
        assertEquals(formVersion, FormUtil.getFormVersionFromFieldPath(formFieldPath));
        verifyCommonMockCalls();

    }

    @Test
    public void shouldReturnFormDetailsFromGivenObsAndFormTypeOfAllObservationTemplates() {
        Concept concept = mock(Concept.class);
        when(obs.getConcept()).thenReturn(concept);
        ConceptName conceptName = mock(ConceptName.class);
        when(concept.getName()).thenReturn(conceptName);
        String obsName = "some obs name";
        when(conceptName.getName()).thenReturn(obsName);

        Collection<FormDetails> formDetailsCollection = FormDetailsMapper
                .createFormDetails(singletonList(obs), FormType.FORMS1);

        assertEquals(1, formDetailsCollection.size());

        FormDetails formDetails = formDetailsCollection.iterator().next();

        assertEquals("v1", formDetails.getFormType());
        assertEquals(formDetails.getFormName(), obsName);
        assertEquals(0, formDetails.getFormVersion());
        verifyCommonData(formDetails);
        verifyCommonMockCalls();

    }

    @Test
    public void shouldReturnFormDetailsWithTwoProvidersFromGivenTwoObsAndFormTypeOfFormBuilder() {

        String anotherObsFormFieldPath = "formName.2/2-0";
        String anotherProviderName = "Another Super Man";
        String anotherProviderUuid = "Another provider-uuid";

        Obs anotherObs = mock(Obs.class);
        User anotherCreator = mock(User.class);
        PersonName anotherPersonName = mock(PersonName.class);

        when(anotherObs.getEncounter()).thenReturn(encounter);
        when(anotherObs.getCreator()).thenReturn(anotherCreator);
        when(anotherObs.getFormFieldPath()).thenReturn(anotherObsFormFieldPath);
//        when(FormUtil.getFormNameFromFieldPath(anotherObsFormFieldPath)).thenReturn(formName);
//        when(FormUtil.getFormVersionFromFieldPath(anotherObsFormFieldPath)).thenReturn(formVersion);

        when(anotherCreator.getPersonName()).thenReturn(anotherPersonName);
        when(anotherPersonName.getFullName()).thenReturn(anotherProviderName);
        when(anotherCreator.getUuid()).thenReturn(anotherProviderUuid);

//        FormType formType = mock(FormType.class);
//        Whitebox.setInternalState(FormType.class, "FORMS2", formType);
//        when(formType.toString()).thenReturn("v2");
        FormType formType = FormType.FORMS2;

        FormDetails formDetails = mock(FormDetails.class);
        when(formDetails.getFormName()).thenReturn(formName);
        when(formDetails.getFormVersion()).thenReturn(2);
        when(formDetails.getEncounterUuid()).thenReturn(encounterUuid);
        Provider provider = mock(Provider.class);
        when(provider.getProviderName()).thenReturn(providerName);
        when(provider.getUuid()).thenReturn(providerUuid);
        when(formDetails.getProviders()).thenReturn(new HashSet<>(singletonList(provider)));

        FormDetails anotherFormDetails = mock(FormDetails.class);
        when(anotherFormDetails.getFormName()).thenReturn(formName);
        when(anotherFormDetails.getFormVersion()).thenReturn(2);
        when(anotherFormDetails.getEncounterUuid()).thenReturn(encounterUuid);
        Provider anotherProvider = mock(Provider.class);
        when(anotherProvider.getProviderName()).thenReturn(anotherProviderName);
        when(anotherProvider.getUuid()).thenReturn(anotherProviderUuid);
        when(anotherFormDetails.getProviders()).thenReturn(new HashSet<>(singletonList(anotherProvider)));


        Collection<FormDetails> formDetailsCollection = FormDetailsMapper
                .createFormDetails(Arrays.asList(obs, anotherObs), formType);

        assertEquals(1, formDetailsCollection.size());

        FormDetails actualFormDetails = formDetailsCollection.iterator().next();
        assertEquals("v2", actualFormDetails.getFormType());
        assertEquals(formName, actualFormDetails.getFormName());
        assertEquals(formVersion, actualFormDetails.getFormVersion());

        verifyVisitAndEncounterData(actualFormDetails);

        verify(obs, times(2)).getFormFieldPath();
        verify(anotherObs, times(2)).getFormFieldPath();
        //verifyStatic(VerificationModeFactory.times(1));
        FormUtil.getFormNameFromFieldPath(formFieldPath);
        //verifyStatic(VerificationModeFactory.times(1));
        FormUtil.getFormNameFromFieldPath(anotherObsFormFieldPath);
        //verifyStatic(VerificationModeFactory.times(1));
        FormUtil.getFormVersionFromFieldPath(formFieldPath);
        //verifyStatic(VerificationModeFactory.times(1));
        FormUtil.getFormVersionFromFieldPath(anotherObsFormFieldPath);
        //verify(formType, times(2)).toString();
    }

    private void verifyCommonData(FormDetails formDetails) {

        verifyVisitAndEncounterData(formDetails);

        assertEquals(1, formDetails.getProviders().size());
        Provider provider = formDetails.getProviders().iterator().next();
        assertEquals(providerName, provider.getProviderName());
        assertEquals(providerUuid, provider.getUuid());
    }

    private void verifyVisitAndEncounterData(FormDetails formDetails) {
        assertEquals(visitStartDateTime, formDetails.getVisitStartDateTime());
        assertEquals(visitUuid, formDetails.getVisitUuid());
        assertEquals(encounterDateTime, formDetails.getEncounterDateTime());
        assertEquals(encounterUuid, formDetails.getEncounterUuid());
    }

    private void verifyCommonMockCalls() {
        verify(obs, times(1)).getEncounter();
        verify(obs, times(1)).getCreator();
        verify(encounter, times(1)).getVisit();
        verify(encounter, times(1)).getUuid();
        verify(encounter, times(1)).getEncounterDatetime();
        verify(visit, times(1)).getUuid();
        verify(visit, times(1)).getStartDatetime();
        verify(anotherCreator, times(1)).getPersonName();
        verify(anotherCreator, times(1)).getUuid();
        verify(personName, times(1)).getFullName();
    }
}