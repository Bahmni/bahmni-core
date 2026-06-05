package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.bahmni.module.bahmnicore.dao.OrderDao;
import org.bahmni.module.bahmnicore.web.v1_0.controller.display.controls.BahmniLabOrderResultController;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResult;
import org.openmrs.module.bahmniemrapi.laborder.contract.LabOrderResults;
import org.openmrs.module.bahmniemrapi.laborder.service.LabOrderResultsService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class BahmniLabOrderResultControllerTest {
    
    @Mock
    private OrderDao orderDao;
    @Mock
    private LabOrderResultsService labOrderResultsService;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void shouldDelegateRetrievingLabOrderResultsToOrderDao() {
        BahmniLabOrderResultController controller = new BahmniLabOrderResultController(null, orderDao, labOrderResultsService);
        Patient patient = new Patient();
        Visit visit = new Visit();
        visit.setPatient(patient);
        List<Visit> expectedVisits = Arrays.asList(visit);

        when(orderDao.getVisitsForUUids(new String[]{"visitUuid1", "visitUuid2"})).thenReturn(expectedVisits);
        LabOrderResults expectedLabOrderResults = new LabOrderResults(new ArrayList<LabOrderResult>());
        when(labOrderResultsService.getAll(patient, expectedVisits, Integer.MAX_VALUE)).thenReturn(expectedLabOrderResults);

        LabOrderResults actualLabOrderResults = controller.getForVisitUuids(new String[]{"visitUuid1", "visitUuid2"});

        assertThat(actualLabOrderResults, is(equalTo(expectedLabOrderResults)));
        verify(orderDao).getVisitsForUUids(new String[]{"visitUuid1", "visitUuid2"});
        verify(labOrderResultsService).getAll(patient,expectedVisits, Integer.MAX_VALUE);
    }
}
