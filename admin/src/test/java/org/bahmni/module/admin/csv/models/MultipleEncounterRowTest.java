package org.bahmni.module.admin.csv.models;

import org.junit.Test;
import org.springframework.util.Assert;

public class MultipleEncounterRowTest {
    @Test
    public void isEmptyReturnsTrueForEmptyRow() {
        Assert.isTrue(new MultipleEncounterRow().getNonEmptyEncounterRows().isEmpty(), "No data in encounter");

        MultipleEncounterRow emptyEncounterRow = new MultipleEncounterRowBuilder().getEmptyMultipleEncounterRow("GAN12345");
        Assert.isTrue(emptyEncounterRow.getNonEmptyEncounterRows().isEmpty(), "No data in encounter");
    }
}