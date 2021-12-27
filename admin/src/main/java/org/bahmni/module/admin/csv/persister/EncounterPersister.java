package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.service.PatientMatchService;
import org.bahmni.module.admin.encounter.BahmniEncounterTransactionImportService;
import org.bahmni.module.admin.retrospectiveEncounter.service.DuplicateObservationService;
import org.openmrs.Patient;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.auditlog.service.AuditLogService;
import org.openmrs.module.bahmniemrapi.drugorder.mapper.BahmniProviderMapper;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.service.BahmniEncounterTransactionService;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
public class EncounterPersister implements EntityPersister<MultipleEncounterRow> {
    public static final String IMPORT_ID = "IMPORT_ID_";
    @Autowired
    private PatientMatchService patientMatchService;
    @Autowired
    private BahmniEncounterTransactionService bahmniEncounterTransactionService;
    @Autowired
    private DuplicateObservationService duplicateObservationService;
    @Autowired
    private BahmniEncounterTransactionImportService bahmniEncounterTransactionImportService;
    @Autowired
    private AuditLogService auditLogService;

    private UserContext userContext;
    private String patientMatchingAlgorithmClassName;
    private boolean shouldMatchExactPatientId;
    private String loginUuid;
    private boolean shouldPerformForm2Validations;

    private static final Logger log = LogManager.getLogger(EncounterPersister.class);

    public void init(UserContext userContext, String patientMatchingAlgorithmClassName, boolean shouldMatchExactPatientId, String loginUuid, boolean shouldPerformForm2Validations) {
        this.userContext = userContext;
        this.patientMatchingAlgorithmClassName = patientMatchingAlgorithmClassName;
        this.shouldMatchExactPatientId = shouldMatchExactPatientId;
        this.loginUuid = loginUuid;
        this.shouldPerformForm2Validations = shouldPerformForm2Validations;
    }

    @Override
    public Messages validate(MultipleEncounterRow multipleEncounterRow) {
        return new Messages();
    }

    @Override
    public Messages persist(MultipleEncounterRow multipleEncounterRow) {
        // This validation is needed as patientservice toString returns all patients for empty patient identifier
        if (StringUtils.isEmpty(multipleEncounterRow.patientIdentifier)) {
            return noMatchingPatients(multipleEncounterRow);
        }
        synchronized ((IMPORT_ID + multipleEncounterRow.patientIdentifier).intern()) {
            try {
                Context.openSession();
                Context.setUserContext(userContext);

                Patient patient = patientMatchService.getPatient(patientMatchingAlgorithmClassName, multipleEncounterRow.patientAttributes,
                        multipleEncounterRow.patientIdentifier, shouldMatchExactPatientId);
                if (patient == null) {
                    return noMatchingPatients(multipleEncounterRow);
                }

                Set<EncounterTransaction.Provider> providers = getProviders(multipleEncounterRow.providerName);

                if(providers.isEmpty()) {
                    return noMatchingProviders(multipleEncounterRow);
                }

                List<BahmniEncounterTransaction> bahmniEncounterTransactions = bahmniEncounterTransactionImportService.getBahmniEncounterTransaction(multipleEncounterRow, patient, shouldPerformForm2Validations);

                for (BahmniEncounterTransaction bahmniEncounterTransaction : bahmniEncounterTransactions) {
                    bahmniEncounterTransaction.setLocationUuid(loginUuid);
                    bahmniEncounterTransaction.setProviders(providers);
                    duplicateObservationService.filter(bahmniEncounterTransaction, patient, multipleEncounterRow.getVisitStartDate(), multipleEncounterRow.getVisitEndDate());
                }
                Boolean isAuditLogEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("bahmni.enableAuditLog"));
                for (BahmniEncounterTransaction bahmniEncounterTransaction : bahmniEncounterTransactions) {
                    BahmniEncounterTransaction updatedBahmniEncounterTransaction = bahmniEncounterTransactionService.save(bahmniEncounterTransaction, patient, multipleEncounterRow.getVisitStartDate(), multipleEncounterRow.getVisitEndDate());
                    if (isAuditLogEnabled) {
                        Map<String, String> params = new HashMap<>();
                        params.put("encounterUuid", updatedBahmniEncounterTransaction.getEncounterUuid());
                        params.put("encounterType", updatedBahmniEncounterTransaction.getEncounterType());
                        auditLogService.createAuditLog(patient.getUuid(), "EDIT_ENCOUNTER", "EDIT_ENCOUNTER_MESSAGE", params, "MODULE_LABEL_ADMIN_KEY");
                    }
                }

                return new Messages();
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                Context.clearSession();
                return new Messages(e);
            } finally {
                Context.flushSession();
                Context.closeSession();
            }
        }
    }

    private Set<EncounterTransaction.Provider> getProviders(String providerName) {
        Set<EncounterTransaction.Provider> encounterTransactionProviders = new HashSet<>();

        if (StringUtils.isEmpty(providerName)) {
            providerName = userContext.getAuthenticatedUser().getUsername();
        }

        User user = Context.getUserService().getUserByUsername(providerName);

        if (user == null){
            return encounterTransactionProviders;
        }

        Collection<Provider> providers = Context.getProviderService().getProvidersByPerson(user.getPerson());

        Set<Provider> providerSet = new HashSet<>(providers);

        BahmniProviderMapper bahmniProviderMapper = new BahmniProviderMapper();

        Iterator iterator = providerSet.iterator();
        while (iterator.hasNext()) {
            encounterTransactionProviders.add(bahmniProviderMapper.map((Provider) iterator.next()));
        }

        return encounterTransactionProviders;
    }

    private Messages noMatchingPatients(MultipleEncounterRow multipleEncounterRow) {
        return new Messages("No matching patients found with ID:'" + multipleEncounterRow.patientIdentifier + "'");
    }

    private Messages noMatchingProviders(MultipleEncounterRow multipleEncounterRow) {
        return new Messages("No matching providers found with username:'" + multipleEncounterRow.providerName + "'");
    }
}