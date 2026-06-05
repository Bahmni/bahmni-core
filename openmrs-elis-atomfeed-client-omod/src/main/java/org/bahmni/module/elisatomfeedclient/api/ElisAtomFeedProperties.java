package org.bahmni.module.elisatomfeedclient.api;

import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.ict4h.atomfeed.client.AtomFeedProperties;
import org.springframework.stereotype.Component;

@Component
public class ElisAtomFeedProperties extends AtomFeedProperties {

    private static final String OPENELIS_URI = "openelis.uri";
    private static final String CONNECT_TIMEOUT = "feed.connectionTimeoutInMilliseconds";
    private static final String MAX_FAILED_EVENTS = "feed.maxFailedEvents";
    private static final String READ_TIMEOUT = "feed.replyTimeoutInMilliseconds";
    private static final String PATIENT_FEED_URI = "patient.feed.uri";
    private static final String OPENELIS_USER = "openelis.user";
    private static final String OPENELIS_PASSWORD = "openelis.password";

    public String getPatientFeedUri() {
        return BahmniCoreProperties.getProperty(PATIENT_FEED_URI);
    }

    public String getOpenElisUser() {
        return BahmniCoreProperties.getProperty(OPENELIS_USER);
    }

    public String getOpenElisPassword() {
        return BahmniCoreProperties.getProperty(OPENELIS_PASSWORD);
    }

    public String getOpenElisUri() {
        return BahmniCoreProperties.getProperty(OPENELIS_URI);
    }

    @Override
    public int getMaxFailedEvents() {
        return Integer.parseInt(BahmniCoreProperties.getProperty(MAX_FAILED_EVENTS));
    }

    @Override
    public int getReadTimeout() {
        return Integer.parseInt(BahmniCoreProperties.getProperty(READ_TIMEOUT));
    }

    @Override
    public int getConnectTimeout() {
        return Integer.parseInt(BahmniCoreProperties.getProperty(CONNECT_TIMEOUT));
    }

}
