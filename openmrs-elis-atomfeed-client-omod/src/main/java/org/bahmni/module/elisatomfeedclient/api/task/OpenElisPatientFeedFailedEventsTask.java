package org.bahmni.module.elisatomfeedclient.api.task;

import org.bahmni.module.elisatomfeedclient.api.client.FailedEventsFeedClient;
import org.bahmni.module.elisatomfeedclient.api.client.OpenElisPatientFailedEventsFeedClient;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

public class OpenElisPatientFeedFailedEventsTask extends AbstractTask {

    @Override
    public void execute() {
        FailedEventsFeedClient feedClient = Context.getService(OpenElisPatientFailedEventsFeedClient.class);
        feedClient.processFailedEvents();
    }
}
