package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.model.Provider;
import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertEquals;

public class BahmniFormDetailsControllerIT extends BaseIntegrationTest {

    @Autowired
    private BahmniFormDetailsController bahmniFormDetailsController;

    private Provider superUser = new Provider();
    private Provider bruno = new Provider();

    @Before
    public void setUp() throws Exception {
        executeDataSet("formBuilderObs.xml");

        superUser.setProviderName("Super User");
        superUser.setUuid("1010d442-e134-11de-babe-001e378eb67e");

        bruno.setProviderName("Bruno Otterbourg");
        bruno.setUuid("c1d8f5c2-e131-11de-babe-001e378eb67e");
    }

    @Test
    public void shouldReturnFormDetailsUsingPatientUuidForFormTypeOfV2() {
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsController
                .getFormDetails("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "v2", -1, null, null);

        assertEquals(2, formDetailsCollection.size());
        Iterator<FormDetails> formDetailsIterator = formDetailsCollection.iterator();
        verifyBloodSampleFormDetails(formDetailsIterator.next());
        verifyVitalFormDetails(formDetailsIterator.next());
    }

    @Test
    public void shouldReturnFormDetailsUsingPatientUuidAndNumberOfNumberOfVisitsForFormTypeOfV2() {
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsController
                .getFormDetails("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "v2", 2, null, null);

        assertEquals(1, formDetailsCollection.size());
        verifyBloodSampleFormDetails(formDetailsCollection.iterator().next());
    }

    @Test
    public void shouldReturnFormDetailsUsingPatientUuidAndVisitUuidForFormTypeOfV2() {
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsController
                .getFormDetails("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "v2", -1, "1e5d5d48-6b78-11e0-93c3-18a905e044dc", null);

        assertEquals(1, formDetailsCollection.size());
        verifyVitalFormDetails(formDetailsCollection.iterator().next());
    }

    @Test
    public void shouldReturnFormDetailsUsingPatientUuidAndPatientProgramUuidForFormTypeOfV2() {
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsController
                .getFormDetails("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "v2", -1, null, "b75462a0-4c92-451e-b8bc-e98b38b76534");

        assertEquals(1, formDetailsCollection.size());
        verifyVitalFormDetails(formDetailsCollection.iterator().next());
    }

    @Test
    public void shouldReturnFormDetailsUsingPatientUuidPatientProgramUuidAndVisitUuidForFormTypeOfV2() {
        Collection<FormDetails> formDetailsCollection = bahmniFormDetailsController
                .getFormDetails("da7f524f-27ce-4bb2-86d6-6d1d05312bd5", "v2", -1, "1e5d5d48-6b78-11e0-93c3-18a905e044dc", "b75462a0-4c92-451e-b8bc-e98b38b76534");

        assertEquals(1, formDetailsCollection.size());
        verifyVitalFormDetails(formDetailsCollection.iterator().next());
    }

    private void verifyBloodSampleFormDetails(FormDetails bloodSampleFormDetails) {
        assertEquals("v2", bloodSampleFormDetails.getFormType());
        assertEquals("BloodSample", bloodSampleFormDetails.getFormName());
        assertEquals(2, bloodSampleFormDetails.getFormVersion());
        assertEquals("fcf11e2c-e59c-11e8-9f32-f2801f1b9fd1", bloodSampleFormDetails.getEncounterUuid());
        assertEquals("2018-11-08 00:10:00.0", bloodSampleFormDetails.getEncounterDateTime().toString());
        assertEquals("4e663d66-6b78-11e0-93c3-18a905e044dc", bloodSampleFormDetails.getVisitUuid());
        assertEquals("2005-01-01 00:00:00.0", bloodSampleFormDetails.getVisitStartDateTime().toString());

        assertEquals(1, bloodSampleFormDetails.getProviders().size());
        assertEquals(superUser, bloodSampleFormDetails.getProviders().iterator().next());
    }

    private void verifyVitalFormDetails(FormDetails vitalsFormDetails) {
        assertEquals("v2", vitalsFormDetails.getFormType());
        assertEquals("Vitals", vitalsFormDetails.getFormName());
        assertEquals(1, vitalsFormDetails.getFormVersion());
        assertEquals("66f59ecc-e59a-11e8-9f32-f2801f1b9fd1", vitalsFormDetails.getEncounterUuid());
        assertEquals("2018-11-08 00:00:00.0", vitalsFormDetails.getEncounterDateTime().toString());
        assertEquals("1e5d5d48-6b78-11e0-93c3-18a905e044dc", vitalsFormDetails.getVisitUuid());
        assertEquals("2005-01-01 00:00:00.0", vitalsFormDetails.getVisitStartDateTime().toString());

        assertEquals(2, vitalsFormDetails.getProviders().size());
        containsInAnyOrder(Arrays.asList(superUser, bruno), vitalsFormDetails.getProviders());
    }


}
