package org.bahmni.module.elisatomfeedclient.api.worker;

import org.ict4h.atomfeed.client.domain.Event;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.Date;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;

public class OpenElisPatientFeedWorkerTest {

    @Mock
    private OpenElisAccessionEventWorker accessionEventWorker;

    @Before
    public void before() {
        initMocks(this);
    }

    @Test
    public void shouldCallAccessionEventWorkerWhenEventTitleIsAccession() {
        OpenElisPatientFeedWorker openElisPatientFeedWorker = new OpenElisPatientFeedWorker(accessionEventWorker);
        Event event = createEvent();
        event.setTitle("accession");

        openElisPatientFeedWorker.process(event);

        verify(accessionEventWorker, times(1)).process(event);
    }

    @Test
    public void shouldNotFailWhenNoEventWorkerFound() {
        OpenElisPatientFeedWorker openElisPatientFeedWorker = new OpenElisPatientFeedWorker(accessionEventWorker);
        Event event = createEvent();
        event.setTitle("newAccession");

        openElisPatientFeedWorker.process(event);

        verify(accessionEventWorker, never()).process(event);
    }

    private Event createEvent() {
        return new Event("tag:atomfeed.ict4h.org:22617bda-71ac-45c0-832b-c945b4881334", "/openelis/ws/rest/accession/4e85095d-b08e-444b-8970-0d5c2210791b","patient", "http://localhost:8080/openelis/ws/feed/patient/recent",new Date());
    }
}
