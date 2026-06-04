package org.bahmni.module.elisatomfeedclient.api.worker;

import org.ict4h.atomfeed.client.domain.Event;
import org.ict4h.atomfeed.client.service.EventWorker;

import java.util.HashMap;

public class OpenElisPatientFeedWorker implements EventWorker {
    public HashMap<String, EventWorker> workers = new HashMap<>();

    public OpenElisPatientFeedWorker(OpenElisAccessionEventWorker accessionEventWorker) {
        workers.put("accession", accessionEventWorker);
    }

    @Override
    public void process(Event event) {
        getEventWorker(event).process(event);
    }

    @Override
    public void cleanUp(Event event) {
        getEventWorker(event).cleanUp(event);
    }

    private EventWorker getEventWorker(Event event) {
        return workerFor(event);
    }

    private EventWorker workerFor(Event event) {
        Object worker = workers.get(event.getTitle());
        return worker == null? new IgnoreEventWorker("No worker found for event: " + event): (EventWorker) worker;
    }
}