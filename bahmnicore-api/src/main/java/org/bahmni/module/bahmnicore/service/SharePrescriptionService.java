package org.bahmni.module.bahmnicore.service;

import org.bahmni.module.bahmnicore.contract.SMS.PrescriptionSMS;
import org.openmrs.annotation.Authorized;
import org.springframework.transaction.annotation.Transactional;

public interface SharePrescriptionService {
    @Transactional(readOnly = true)
    @Authorized({"Send Prescription SMS"})
    Object sendPresciptionSMS(PrescriptionSMS prescription);
}
