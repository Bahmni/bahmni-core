package org.bahmni.module.admin.csv.service;

import org.bahmni.csv.KeyValue;
import org.bahmni.form2.service.FormFieldPathService;
import org.bahmni.module.admin.csv.models.SectionPositionValue;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@PrepareForTest(CSVObservationHelper.class)
@RunWith(PowerMockRunner.class)
public class FormFieldPathGeneratorServiceTest {

    private FormFieldPathService formFieldPathService;
    private FormFieldPathGeneratorService formFieldPathGeneratorService;

    @Before
    public void setUp() {
        formFieldPathService = mock(FormFieldPathService.class);
        formFieldPathGeneratorService = new FormFieldPathGeneratorService(formFieldPathService);
    }

    @Test
    public void shouldNotSetFormFieldPathForEmptyObserevations() {
        List<EncounterTransaction.Observation> form2Observations = asList();
        List<String> form2CSVHeaderParts = null;
        formFieldPathGeneratorService.setFormNamespaceAndFieldPath(form2Observations, form2CSVHeaderParts);

        assertEquals(0, form2Observations.size());
    }

    @Test
    public void shouldSetFormFieldPathForObserevation() {
        EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
        observation.setUuid("UUID");
        observation.setConcept(new EncounterTransaction.Concept());
        observation.setValue("Dummy Value");

        List<EncounterTransaction.Observation> form2Observations = asList(observation);
        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Vitals", "Height"));

        PowerMockito.mockStatic(CSVObservationHelper.class);
        when(CSVObservationHelper.getLastItem(anyListOf(EncounterTransaction.Observation.class))).thenReturn(observation);
        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Vitals.1/1-0");

        formFieldPathGeneratorService.setFormNamespaceAndFieldPath(form2Observations, headerParts);

        assertEquals("Vitals.1/1-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
    }

    @Test
    public void shouldSetFormFieldPathForMultiSelectObserevation() {
        EncounterTransaction.Observation observation1 = new EncounterTransaction.Observation();
        observation1.setUuid("UUID1");
        observation1.setConcept(new EncounterTransaction.Concept());
        observation1.setValue("Cough");

        EncounterTransaction.Observation observation2 = new EncounterTransaction.Observation();
        observation2.setUuid("UUID2");
        observation2.setConcept(new EncounterTransaction.Concept());
        observation2.setValue("Fever");

        final KeyValue obs1 = new KeyValue("Covid.Symptoms", "Cough");
        final KeyValue obs2 = new KeyValue("Covid.Symptoms", "Fever");

        List<EncounterTransaction.Observation> form2Observations = asList(observation1, observation2);
        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Covid", "Symptoms"));

        PowerMockito.mockStatic(CSVObservationHelper.class);
        when(CSVObservationHelper.getLastItem(anyListOf(EncounterTransaction.Observation.class))).thenReturn(observation1);
        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Covid.1/1-0");

        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForMultiSelectObs(form2Observations, headerParts, asList(obs1,obs2));

        assertEquals("Covid.1/1-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Covid.1/1-0", form2Observations.get(1).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(1).getFormNamespace());
    }

    @Test
    public void shouldSetFormFieldPathForAddmoreObserevation() {
        EncounterTransaction.Observation observation1 = new EncounterTransaction.Observation();
        observation1.setUuid("UUID1");
        observation1.setConcept(new EncounterTransaction.Concept());
        observation1.setValue("Cough");

        EncounterTransaction.Observation observation2 = new EncounterTransaction.Observation();
        observation2.setUuid("UUID2");
        observation2.setConcept(new EncounterTransaction.Concept());
        observation2.setValue("Fever");

        final KeyValue obs1 = new KeyValue("Covid.Symptoms", "Cough");
        final KeyValue obs2 = new KeyValue("Covid.Symptoms", "Fever");

        List<EncounterTransaction.Observation> form2Observations = asList(observation1, observation2);
        final List<String> headerParts = new ArrayList<>(Arrays.asList("form2", "Covid", "Symptoms"));

        PowerMockito.mockStatic(CSVObservationHelper.class);
        when(CSVObservationHelper.getLastItem(anyListOf(EncounterTransaction.Observation.class))).thenReturn(observation1);
        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Covid.1/1-0");

        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForAddmoreObs(form2Observations, headerParts, asList(obs1,obs2));

        assertEquals("Covid.1/1-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Covid.1/1-1", form2Observations.get(1).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(1).getFormNamespace());
    }

    @Test
    public void shouldSetFormFieldPathForObservationInsideAddmoreSection() {
        EncounterTransaction.Observation observation1 = new EncounterTransaction.Observation();
        observation1.setUuid("UUID1");
        observation1.setConcept(new EncounterTransaction.Concept());
        observation1.setValue("Male");

        EncounterTransaction.Observation observation2 = new EncounterTransaction.Observation();
        observation2.setUuid("UUID2");
        observation2.setConcept(new EncounterTransaction.Concept());
        observation2.setValue("Female");

        KeyValue csvObs1 = new KeyValue("form2.Birth Details.Infant Details.Infant Gender", "Male");
        KeyValue csvObs2 = new KeyValue("form2.Birth Details.Infant Details.Infant Gender", "Female");

        SectionPositionValue sectionPositionValue1 = new SectionPositionValue("Male", "0", 0, -1, -1);
        SectionPositionValue sectionPositionValue2 = new SectionPositionValue("Female", "0", 1, -1, -1);

        List<EncounterTransaction.Observation> form2Observations = asList(observation1, observation2);
        List<String> headerParts = asList("form2", "Birth Details", "Infant Details", "Infant Gender");
        List<KeyValue> csvObservations = asList(csvObs1, csvObs2);
        List<SectionPositionValue> sectionPositionValuesList = asList(sectionPositionValue1, sectionPositionValue2);

        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Birth Details.1/1-0/10-0");
        when(formFieldPathService.isAddmore(headerParts.subList(0,3))).thenReturn(true);

        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForJsonValue(form2Observations, headerParts, csvObservations, sectionPositionValuesList);

        assertEquals("Birth Details.1/1-0/10-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Birth Details.1/1-1/10-0", form2Observations.get(1).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(1).getFormNamespace());
    }

    @Test
    public void shouldSetFormFieldPathForObservationInsideNestedAddmoreSection() {
        List<EncounterTransaction.Observation> form2Observations = new ArrayList<>();
        List<String> obsInJson = asList("Xpert Qual/DNA PCR", "Antibody Test (RDT)", "Ultrasensitive AgP24", "Xpert Qual/DNA PCR", "Not specified");
        obsInJson.stream().forEach(obs -> {
            EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
            observation.setUuid("UUID" + obs);
            observation.setConcept(new EncounterTransaction.Concept());
            observation.setValue(obs);
            form2Observations.add(observation);
        });

        KeyValue csvObs1 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Xpert Qual/DNA PCR");
        KeyValue csvObs2 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Antibody Test (RDT)");
        KeyValue csvObs3 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Ultrasensitive AgP24");
        KeyValue csvObs4 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Xpert Qual/DNA PCR");
        KeyValue csvObs5 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Not specified");

        SectionPositionValue sectionPositionValue1 = new SectionPositionValue("Xpert Qual/DNA PCR", "0/0", 0, -1, -1);
        SectionPositionValue sectionPositionValue2 = new SectionPositionValue("Antibody Test (RDT)", "0/0", 1, -1, -1);
        SectionPositionValue sectionPositionValue3 = new SectionPositionValue("Ultrasensitive AgP24", "0/1", 0, -1, -1);
        SectionPositionValue sectionPositionValue4 = new SectionPositionValue("Xpert Qual/DNA PCR", "0/1", 1, -1, -1);
        SectionPositionValue sectionPositionValue5 = new SectionPositionValue("Not specified", "0/1", 2, -1, -1);

        List<String> headerParts = asList("form2", "Birth Details", "Infant Details", "HIV Assessments", "Infant interim HIV test type");
        List<KeyValue> csvObservations = asList(csvObs1, csvObs2, csvObs3, csvObs4, csvObs5);
        List<SectionPositionValue> sectionPositionValuesList = asList(sectionPositionValue1, sectionPositionValue2, sectionPositionValue3, sectionPositionValue4, sectionPositionValue5);

        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Birth Details.1/17-0/31-0/57-0");
        when(formFieldPathService.isAddmore(headerParts.subList(0,2))).thenReturn(true);
        when(formFieldPathService.isAddmore(headerParts.subList(0,3))).thenReturn(true);

        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForJsonValue(form2Observations, headerParts, csvObservations, sectionPositionValuesList);

        assertEquals("Birth Details.1/17-0/31-0/57-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Birth Details.1/17-0/31-1/57-0", form2Observations.get(1).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-0/57-0", form2Observations.get(2).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-1/57-0", form2Observations.get(3).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-2/57-0", form2Observations.get(4).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(1).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(2).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(3).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(4).getFormNamespace());
    }

    @Test
    public void shouldSetFormFieldPathForAddmoreObservationInsideNestedAddmoreSection() {
        List<EncounterTransaction.Observation> form2Observations = new ArrayList<>();
        List<String> obsInJson = asList("Xpert Qual/DNA PCR", "Antibody Test (RDT)", "Ultrasensitive AgP24", "Xpert Qual/DNA PCR", "Not specified");
        obsInJson.stream().forEach(obs -> {
            EncounterTransaction.Observation observation = new EncounterTransaction.Observation();
            observation.setUuid("UUID" + obs);
            observation.setConcept(new EncounterTransaction.Concept());
            observation.setValue(obs);
            form2Observations.add(observation);
        });

        KeyValue csvObs1 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Xpert Qual/DNA PCR");
        KeyValue csvObs2 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Antibody Test (RDT)");
        KeyValue csvObs3 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Ultrasensitive AgP24");
        KeyValue csvObs4 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Xpert Qual/DNA PCR");
        KeyValue csvObs5 = new KeyValue("form2.Birth Details.Infant Details.HIV Assessments.Infant interim HIV test type", "Not specified");

        SectionPositionValue sectionPositionValue1 = new SectionPositionValue("Xpert Qual/DNA PCR", "0/0", 0, -1, 0);
        SectionPositionValue sectionPositionValue2 = new SectionPositionValue("Antibody Test (RDT)", "0/0", 0, -1, 1);
        SectionPositionValue sectionPositionValue3 = new SectionPositionValue("Ultrasensitive AgP24", "0/1", 0, -1, 0);
        SectionPositionValue sectionPositionValue4 = new SectionPositionValue("Xpert Qual/DNA PCR", "0/1", 0, -1, 1);
        SectionPositionValue sectionPositionValue5 = new SectionPositionValue("Not specified", "0/1", 0, -1, 2);

        List<String> headerParts = asList("form2", "Birth Details", "Infant Details", "HIV Assessments", "Infant interim HIV test type");
        List<KeyValue> csvObservations = asList(csvObs1, csvObs2, csvObs3, csvObs4, csvObs5);
        List<SectionPositionValue> sectionPositionValuesList = asList(sectionPositionValue1, sectionPositionValue2, sectionPositionValue3, sectionPositionValue4, sectionPositionValue5);

        when(formFieldPathService.getFormFieldPath(anyListOf(String.class))).thenReturn("Birth Details.1/17-0/31-0/57-0");
        when(formFieldPathService.isAddmore(headerParts.subList(0,2))).thenReturn(true);
        when(formFieldPathService.isAddmore(headerParts.subList(0,3))).thenReturn(true);

        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForJsonValue(form2Observations, headerParts, csvObservations, sectionPositionValuesList);

        assertEquals("Birth Details.1/17-0/31-0/57-0", form2Observations.get(0).getFormFieldPath());
        assertEquals("Birth Details.1/17-0/31-0/57-1", form2Observations.get(1).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-0/57-0", form2Observations.get(2).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-0/57-1", form2Observations.get(3).getFormFieldPath());
        assertEquals("Birth Details.1/17-1/31-0/57-2", form2Observations.get(4).getFormFieldPath());
        assertEquals("Bahmni", form2Observations.get(0).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(1).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(2).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(3).getFormNamespace());
        assertEquals("Bahmni", form2Observations.get(4).getFormNamespace());
    }
}
