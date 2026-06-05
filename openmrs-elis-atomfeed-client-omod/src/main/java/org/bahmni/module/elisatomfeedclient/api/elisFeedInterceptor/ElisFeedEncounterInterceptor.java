package org.bahmni.module.elisatomfeedclient.api.elisFeedInterceptor;

import org.openmrs.Encounter;

import java.util.Set;

public interface ElisFeedEncounterInterceptor {
    void run(Set<Encounter> encounters);
}
