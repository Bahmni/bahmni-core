package org.bahmni.module.elisatomfeedclient.api.command;

import org.openmrs.Encounter;

import java.util.List;

public interface ELISResultPostSaveCommand {
    void onResult(List<Encounter> encounters);
}
