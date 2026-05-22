package org.bahmni.module.bahmnicore.task;

import org.bahmni.module.bahmnicore.service.FormDraftService;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiscardAllFormDraftsTask extends AbstractTask {

    private static final Logger log = LoggerFactory.getLogger(DiscardAllFormDraftsTask.class);

    @Override
    public void execute() {
        try {
            log.info("DiscardAllFormDraftsTask: starting midnight discard of all form drafts");
            FormDraftService formDraftService = Context.getService(FormDraftService.class);
            formDraftService.discardAllDrafts();
            log.info("DiscardAllFormDraftsTask: completed successfully");
        } catch (Exception e) {
            log.error("DiscardAllFormDraftsTask: failed to discard all form drafts", e);
        }
    }
}
