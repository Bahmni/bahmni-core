package org.bahmni.module.admin.csv.persister;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.ReferenceTermRow;
import org.bahmni.module.referencedata.labconcepts.service.ReferenceDataConceptReferenceTermService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ReferenceTermPersister implements EntityPersister<ReferenceTermRow> {

    private UserContext userContext;
    private static final Logger log = LogManager.getLogger(PatientPersister.class);

    @Autowired
    private ReferenceDataConceptReferenceTermService referenceDataConceptReferenceTermService;

    public void init(UserContext userContext) {
        this.userContext = userContext;
    }

    @Override
    public Messages persist(ReferenceTermRow referenceTermRow) {
        try {
            Context.openSession();
            Context.setUserContext(userContext);

            referenceDataConceptReferenceTermService.saveOrUpdate(getConceptReferenceTerm(referenceTermRow));
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }
        return new Messages();
    }

    private org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm getConceptReferenceTerm(ReferenceTermRow referenceTermRow) {
        return new org.bahmni.module.referencedata.labconcepts.contract.ConceptReferenceTerm(
                referenceTermRow.getCode(),
                referenceTermRow.getName(),
                null,
                referenceTermRow.getSource(),
                referenceTermRow.getDescription(),
                referenceTermRow.getVersion()
        );
    }

    @Override
    public Messages validate(ReferenceTermRow referenceTermRow) {
        return new Messages();
    }
}
