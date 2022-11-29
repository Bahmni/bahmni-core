package org.bahmni.module.admin.csv.persister;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.admin.csv.service.CSVRelationshipService;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PersonService;
import org.openmrs.api.ProviderService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.text.ParseException;

@Component
public class RelationshipPersister implements EntityPersister<RelationshipRow> {

    @Autowired
    private BahmniPatientService patientService;

    @Autowired
    private ProviderService providerService;

    @Autowired
    private PersonService personService;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private static final Logger log = LogManager.getLogger(RelationshipPersister.class);
    private UserContext userContext;

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Messages validate(RelationshipRow relationshipRow) {
        return new Messages();
    }

    void validateRow(RelationshipRow relationshipRow){
        if (StringUtils.isEmpty(relationshipRow.getPatientIdentifier())) {
            throw new RuntimeException("Patient unique identifier not specified.");
        }

        if (StringUtils.isEmpty(relationshipRow.getPatientRelationIdentifier()) && StringUtils.isEmpty(relationshipRow.getProviderName())) {
            throw new RuntimeException("Both Provider Name and Relation Identifier cannot be null.");
        }

        if (StringUtils.isEmpty(relationshipRow.getRelationshipType())) {
            throw new RuntimeException("Relationship type is not specified.");
        }

        if ((!StringUtils.isEmpty(relationshipRow.getStartDate()) && !StringUtils.isEmpty(relationshipRow.getEndDate()))) {
            try {
                if (CSVUtils.getDateFromString(relationshipRow.getStartDate()).after(CSVUtils.getDateFromString(relationshipRow.getEndDate()))){
                    throw new RuntimeException("Start date should be before end date.");
                }
            } catch (ParseException e) {
                throw new RuntimeException("Could not parse provided dates. Please provide date in format yyyy-mm-dd");
            }
        }
    }

    @Override
    public Messages persist(RelationshipRow relationshipRow) {
        try {
            Context.openSession();
            Context.setUserContext(userContext);
            validateRow(relationshipRow);
            new CSVRelationshipService(patientService, personService, providerService, administrationService).save(relationshipRow);

            return new Messages();
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }

    }

}

