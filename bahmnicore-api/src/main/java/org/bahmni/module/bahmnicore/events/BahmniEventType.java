package org.bahmni.module.bahmnicore.events;

public enum BahmniEventType {
    BAHMNI_PATIENT_CREATED("bahmni-patient"),
    BAHMNI_PATIENT_UPDATED("bahmni-patient"),
    BAHMNI_ENCOUNTER_CREATED("bahmni-encounter"),
    BAHMNI_ENCOUNTER_UPDATED("bahmni-encounter");

    private final String topic;
    BahmniEventType(String topic) {
        this.topic = topic;
    }
    public String topic() {
        return topic;
    }
}