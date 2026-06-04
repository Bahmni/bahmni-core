package org.bahmni.module.bahmnicore.model.error;

public class ErrorCode {
    public static int DuplicatePatient = 1;
    public static int DuplicateCustomer = 2;
    public static int OpenERPError = 3;
    public static int OpenMRSError = 4;

    public static boolean duplicationError(int errorCode) {
        return errorCode == DuplicateCustomer || errorCode == DuplicatePatient;
    }
}
