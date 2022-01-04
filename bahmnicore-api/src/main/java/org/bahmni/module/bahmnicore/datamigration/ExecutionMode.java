package org.bahmni.module.bahmnicore.datamigration;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.ApplicationError;
import org.bahmni.module.bahmnicore.BahmniCoreException;
import org.bahmni.module.bahmnicore.model.BahmniPatient;
import org.bahmni.module.bahmnicore.model.error.ErrorCode;
import org.bahmni.module.bahmnicore.model.error.ErrorMessage;

public class ExecutionMode {
    private final boolean dataMigrationMode;
    private static Logger logger = LogManager.getLogger(ExecutionMode.class);

    public ExecutionMode(String dataMigrationProperty) {
        dataMigrationMode = !(dataMigrationProperty == null || !Boolean.parseBoolean(dataMigrationProperty));
    }

    private void handleFailure(BahmniPatient bahmniPatient, ApplicationError applicationError) {
        if (!dataMigrationMode) {
            throw applicationError;
        }

        if (ErrorCode.duplicationError(applicationError.getErrorCode()))
            logger.warn(applicationError.getMessage(), bahmniPatient.getIdentifier());
        else
            throw applicationError;
    }

    public void handleSavePatientFailure(RuntimeException e, BahmniPatient bahmniPatient) {
        if(e.getMessage() != null){
            int errorCode = e.getMessage().contains(ErrorMessage.ExistingPatientMessagePart) ? ErrorCode.DuplicatePatient : ErrorCode.OpenMRSError;
            BahmniCoreException bahmniCoreException = new BahmniCoreException("Create patient failed", e);
            bahmniCoreException.setErrorCode(errorCode);
            handleFailure(bahmniPatient, bahmniCoreException);
        }
    }
}