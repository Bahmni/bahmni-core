package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.bahmni.module.bahmnicore.forms2.service.BahmniFormDetailsService;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BahmniFormDetailsControllerTest {

    private BahmniFormDetailsController bahmniFormDetailsController;

    private BahmniFormDetailsService bahmniFormDetailsService = mock(BahmniFormDetailsService.class);

    private String patientUuid = "provider-uuid";
    private String formType = "v2";
    private String visitUuid = "visitUuid";
    private String patientProgramUuid = "patientProgramUuid";

    @Before
    public void setUp() {
        bahmniFormDetailsController = new BahmniFormDetailsController(bahmniFormDetailsService);
    }

    @Test
    public void shouldReturnCollectionOfFormDetailsGivenPatienUuidFormTypeAndNumberOfVisits() {
        FormDetails formDetails = mock(FormDetails.class);
        when(bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, -1))
                .thenReturn(Collections.singletonList(formDetails));

        Collection<FormDetails> actualFormDetails = bahmniFormDetailsController.getFormDetails(patientUuid, formType, -1, null, null);

        assertFormDetails(formDetails, actualFormDetails);
        verify(bahmniFormDetailsService, times(1)).getFormDetails(patientUuid, FormType.FORMS2, -1);
    }

    private void assertFormDetails(FormDetails formDetails, Collection<FormDetails> actualFormDetails) {
        assertEquals(1, actualFormDetails.size());
        assertEquals(formDetails, actualFormDetails.iterator().next());
    }

    @Test
    public void shouldReturnCollectionOfFormDetailsGivenPatientUuidFormTypeVisitUuidAndPatientProgramUuid() {
        FormDetails formDetails = mock(FormDetails.class);

        when(bahmniFormDetailsService.getFormDetails(patientUuid, FormType.FORMS2, visitUuid, patientProgramUuid))
                .thenReturn(Collections.singletonList(formDetails));

        Collection<FormDetails> actualFormDetails = bahmniFormDetailsController.getFormDetails(patientUuid, formType, -1, visitUuid, patientProgramUuid);

        assertFormDetails(formDetails, actualFormDetails);
        verify(bahmniFormDetailsService, times(1)).getFormDetails(patientUuid, FormType.FORMS2, visitUuid, patientProgramUuid);
    }
}