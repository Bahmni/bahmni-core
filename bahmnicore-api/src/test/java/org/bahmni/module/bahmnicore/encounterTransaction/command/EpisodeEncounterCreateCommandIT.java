package org.bahmni.module.bahmnicore.encounterTransaction.command;

import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.service.BahmniProgramWorkflowService;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.PatientProgram;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.episodes.Episode;
import org.openmrs.module.episodes.service.EpisodeService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertTrue;

public class EpisodeEncounterCreateCommandIT extends BaseIntegrationTest {

    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;

    @Autowired
    private EncounterService encounterService;

    @Autowired
    private EpisodeService episodeService;

    private BahmniProgramWorkflowService bahmniProgramWorkflowService;

    @Before
    public void setUp() throws Exception {
        bahmniProgramWorkflowService = Context.getService(BahmniProgramWorkflowService.class);
        executeDataSet("visitAttributeDataSet.xml");
    }

    @Test
    public void shouldAddEncounterToEpisodeWhenProgramUuidIsSpecified() {
        String visitUuid = "4e663d66-6b78-11e0-93c3-18a905e044dc";
        String patientUuid = "da7f524f-27ce-4bb2-86d6-6d1d05312bd5";
        String patientProgramUuid = "b75462a0-4c92-451e-b8bc-e98b38b76534"; //for patient 2 in standardDataset.xml

        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setUuid(Context.getProviderService().getProvider(1).getUuid());
        Set<EncounterTransaction.Provider> providerSet = new HashSet<>();
        providerSet.add(provider);

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        bahmniEncounterTransaction.setProviders(providerSet);

        bahmniEncounterTransaction.setEncounterTypeUuid("07000be2-26b6-4cce-8b40-866d8435b613");
        bahmniEncounterTransaction.setPatientUuid(patientUuid);
        bahmniEncounterTransaction.setVisitUuid(visitUuid);

        bahmniEncounterTransaction.setPatientProgramUuid(patientProgramUuid);

        BahmniEncounterTransaction encounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction);


        Encounter encounter = encounterService
                .getEncounterByUuid(encounterTransaction.getEncounterUuid());

        PatientProgram patientProgram = bahmniProgramWorkflowService.getPatientProgramByUuid(patientProgramUuid);
        Episode episodeForPatientProgram = episodeService.getEpisodeForPatientProgram(patientProgram);

        assertTrue(episodeForPatientProgram.getEncounters().contains(encounter));
        assertTrue(episodeForPatientProgram.getPatientPrograms().contains(patientProgram));
    }
}