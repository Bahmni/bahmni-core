package org.bahmni.module.bahmnicore.contract.visit;

public enum EncounterType {
    ADMISSION ("ADMISSION"),
    DISHCARGE ("DISCHARGE");

    private final String name;

    EncounterType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}