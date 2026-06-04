package org.bahmni.module.elisatomfeedclient.api.task;

import org.bahmni.module.elisatomfeedclient.api.client.FeedClient;
import org.bahmni.module.elisatomfeedclient.api.client.OpenElisPatientFeedClient;
import org.openmrs.api.context.Context;
import org.openmrs.scheduler.tasks.AbstractTask;

public class OpenElisPatientFeedTask extends AbstractTask {

    @Override
    public void execute() {
        FeedClient feedClient = Context.getService(OpenElisPatientFeedClient.class);
        feedClient.processFeed();
    }
}
