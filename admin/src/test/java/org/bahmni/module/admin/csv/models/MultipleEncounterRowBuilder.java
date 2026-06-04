package org.bahmni.module.admin.csv.models;

import org.bahmni.csv.KeyValue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MultipleEncounterRowBuilder {
    public MultipleEncounterRow getEmptyMultipleEncounterRow(String patientId) {
        List<KeyValue> emptyDiagnoses = new ArrayList<>();
        emptyDiagnoses.add(new KeyValue("diagnosis", " "));
        emptyDiagnoses.add(new KeyValue("diagnosis", " "));

        List<KeyValue> emptyObservations = new ArrayList<>();
        emptyObservations.add(new KeyValue("diagnosis", " "));
        emptyObservations.add(new KeyValue("diagnosis", " "));

        EncounterRow emptyEncounterRow = new EncounterRow();
        emptyEncounterRow.encounterDateTime = " ";
        emptyEncounterRow.obsRows = emptyObservations;
        emptyEncounterRow.diagnosesRows = emptyDiagnoses;

        MultipleEncounterRow multipleEncounterRow = new MultipleEncounterRow();
        multipleEncounterRow.patientIdentifier = patientId;
        multipleEncounterRow.encounterRows = Arrays.asList(emptyEncounterRow);
        return multipleEncounterRow;
    }

}