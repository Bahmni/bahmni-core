package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.web.v1_0.BaseIntegrationTest;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class BahmniDispositionControllerIT extends BaseIntegrationTest {

    @Autowired
    private BahmniDispositionController bahmniDispositionController;

    @Before
    public void setUp() throws Exception {
        executeDataSet("diagnosisMetadata.xml");
        executeDataSet("dispositionsForVisit.xml");
    }

    @Test
    public void shouldRetrieveEmptyDispositionsByInvalidVisitUuid(){
        List<BahmniDisposition> dispositions = bahmniDispositionController.getDispositionByVisitUuid("INVALID");
        assertEquals(dispositions.size(),0);
    }

    @Test
    public void shouldRetrieveDispositionForVisitUuid(){
        List<BahmniDisposition> dispositions = bahmniDispositionController.getDispositionByVisitUuid("adf4fb41-a41a-4ad6-12f5-2f59889acf5a");
        assertEquals(dispositions.size(),1);
        assertEquals("Absconding", dispositions.get(0).getConceptName());
    }

    @Test
    public void shouldRetrieveDispositionByPatient(){
        List<BahmniDisposition> dispositions = bahmniDispositionController.getDispositionByPatientUuid("1a246ed5-3c11-11de-a0ba-001ed98eb67a", 3);
        assertEquals(dispositions.size(),1);
        assertEquals("Absconding", dispositions.get(0).getConceptName());
    }

}
