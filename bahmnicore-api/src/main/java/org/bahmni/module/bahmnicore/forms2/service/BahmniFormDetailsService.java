package org.bahmni.module.bahmnicore.forms2.service;

import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.contract.FormType;

import java.util.Collection;

public interface BahmniFormDetailsService {
    Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, int numberOfVisits);

    Collection<FormDetails> getFormDetails(String patientUuid, FormType formType, String visitUuid, String patientProgramUuid);
}
