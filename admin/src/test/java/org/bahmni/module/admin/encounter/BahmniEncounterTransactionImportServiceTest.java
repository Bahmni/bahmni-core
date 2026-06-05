package org.bahmni.module.admin.encounter;

import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.models.MultipleEncounterRowBuilder;
import org.junit.Test;
import org.openmrs.EncounterType;
import org.openmrs.api.EncounterService;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.springframework.util.Assert;

import java.text.ParseException;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BahmniEncounterTransactionImportServiceTest {
    @Test
    public void returnEmptyEncounterTransactionForEmptyEncounterRow() throws ParseException {
        EncounterService mockEncounterService = mock(EncounterService.class);
        when(mockEncounterService.getEncounterType("Consultation")).thenReturn(new EncounterType());

        BahmniEncounterTransactionImportService bahmniEncounterTransactionImportService = new BahmniEncounterTransactionImportService(mockEncounterService, null, null, null);
        MultipleEncounterRow emptyEncounterRow = new MultipleEncounterRowBuilder().getEmptyMultipleEncounterRow("GAN12345");
        emptyEncounterRow.encounterType = "Consultation";
        List<BahmniEncounterTransaction> bahmniEncounterTransaction = bahmniEncounterTransactionImportService.getBahmniEncounterTransaction(emptyEncounterRow, null, false);
        Assert.isTrue(bahmniEncounterTransaction.isEmpty(), "Should ignore empty encounters");

        bahmniEncounterTransaction = bahmniEncounterTransactionImportService.getBahmniEncounterTransaction(new MultipleEncounterRow(), null, false);
        Assert.isTrue(bahmniEncounterTransaction.isEmpty(), "Should ignore empty encounters");
    }
}