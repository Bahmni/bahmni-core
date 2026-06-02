/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at https://www.bahmni.org/license/mplv2hd.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

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