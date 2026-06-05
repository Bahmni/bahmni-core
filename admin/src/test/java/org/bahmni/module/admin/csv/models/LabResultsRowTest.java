package org.bahmni.module.admin.csv.models;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.hasItems;

public class LabResultsRowTest {

    @Test
    public void testGetTestResultsReturnsNonEmptyTestResults() throws Exception {
        LabResultRow labResultRow1 = new LabResultRow(null, null);
        LabResultRow labResultRow2 = new LabResultRow("", "");
        LabResultRow labResultRow3 = new LabResultRow("", null);
        LabResultRow labResultRow4 = new LabResultRow("HB1Ac", null);
        LabResultRow labResultRow5 = new LabResultRow("HB1Ac", "10");
        LabResultRow labResultRow6 = new LabResultRow("", "10");
        List<LabResultRow> allLabResultRows = Arrays.asList(labResultRow1, labResultRow2, labResultRow3, labResultRow4, labResultRow5, labResultRow6);
        LabResultsRow labResultsRow = new LabResultsRow();
        labResultsRow.setTestResults(allLabResultRows);

        List<LabResultRow> testResults = labResultsRow.getTestResults();

        assertEquals(3, testResults.size());
        assertThat(testResults, hasItems(labResultRow4, labResultRow5, labResultRow6));
    }
}