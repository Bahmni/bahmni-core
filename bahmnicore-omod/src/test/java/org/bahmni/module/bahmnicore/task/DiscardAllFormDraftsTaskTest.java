package org.bahmni.module.bahmnicore.task;

import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class DiscardAllFormDraftsTaskTest {

    @Mock
    private FormDraftService formDraftService;

    private DiscardAllFormDraftsTask task;

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getService(FormDraftService.class)).thenReturn(formDraftService);
        task = new DiscardAllFormDraftsTask();
    }

    @Test
    public void execute_shouldDiscardAllDraftsAndDeleteOldDrafts() {
        task.execute();

        verify(formDraftService).discardAllDrafts();
        verify(formDraftService).deleteDraftsOlderThanRetentionPeriod();
    }

    @Test
    public void execute_shouldHandleExceptionGracefully() {
        doThrow(new RuntimeException("DB error")).when(formDraftService).discardAllDrafts();

        task.execute();

        // Should not propagate exception - just log it
        verify(formDraftService).discardAllDrafts();
        verify(formDraftService, never()).deleteDraftsOlderThanRetentionPeriod();
    }

    @Test
    public void execute_shouldHandleExceptionInDeleteDraftsOlderThanRetentionPeriod() {
        doThrow(new RuntimeException("Retention error")).when(formDraftService).deleteDraftsOlderThanRetentionPeriod();

        task.execute();

        verify(formDraftService).discardAllDrafts();
        verify(formDraftService).deleteDraftsOlderThanRetentionPeriod();
    }
}
