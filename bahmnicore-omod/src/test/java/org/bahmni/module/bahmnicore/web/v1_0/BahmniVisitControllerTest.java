package org.bahmni.module.bahmnicore.web.v1_0;

import org.bahmni.module.bahmnicore.web.v1_0.controller.BahmniVisitController;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class BahmniVisitControllerTest {
    @Mock
    private VisitService visitService;
    @Mock
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @InjectMocks
    private BahmniVisitController bahmniVisitController;

    @Test
    public void shouldCloseExistingVisitAndCreateANewEncounter() {
        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        Visit visit = new Visit();
        when(visitService.getVisitByUuid("visitUuid")).thenReturn(visit);

        bahmniVisitController.endVisitAndCreateNewEncounter("visitUuid", bahmniEncounterTransaction);

        verify(visitService, times(1)).getVisitByUuid("visitUuid");
        verify(visitService, times(1)).endVisit(visit, null);
        verify(bahmniEncounterTransactionService, times(1)).save(bahmniEncounterTransaction);
    }

}
