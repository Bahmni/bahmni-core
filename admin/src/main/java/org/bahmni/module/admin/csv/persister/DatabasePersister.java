package org.bahmni.module.admin.csv.persister;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.CSVEntity;
import org.bahmni.csv.EntityPersister;
import org.bahmni.csv.Messages;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;

public class DatabasePersister<T extends CSVEntity> implements EntityPersister<T> {
    private final EntityPersister<T> persister;
    private final UserContext userContext;
    private static final Logger log = LogManager.getLogger(DatabasePersister.class);

    public DatabasePersister(EntityPersister<T> persister) {
        this.persister = persister;
        userContext = Context.getUserContext();
    }

    @Override
    public Messages persist(T csvEntity) {
        try {
            Context.openSession();
            Context.setUserContext(userContext);
            return persister.persist(csvEntity);
        } catch (Throwable e) {
            log.error(e.getMessage(), e);
            Context.clearSession();
            return new Messages(e);
        } finally {
            Context.flushSession();
            Context.closeSession();
        }
    }

    @Override
    public Messages validate(T csvEntity) {
        return persister.validate(csvEntity);
    }
}
