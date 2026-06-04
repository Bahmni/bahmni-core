package org.bahmni.module.bahmnicore.forms2.mapper;

import org.bahmni.module.bahmnicore.forms2.contract.FormType;
import org.bahmni.module.bahmnicore.forms2.contract.FormDetails;
import org.bahmni.module.bahmnicore.forms2.util.FormUtil;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.User;
import org.openmrs.Visit;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

public class FormDetailsMapper {

    public static Collection<FormDetails> createFormDetails(List<Obs> observations, FormType formType) {
        HashMap<FormDetails, FormDetails> formDetailsMap = new HashMap<>();
        observations.forEach(obs -> {
            FormDetails formDetails = FormDetailsMapper.map(obs, formType);
            addMultipleProvidersOfAForm(formDetailsMap, formDetails);
        });
        return formDetailsMap.keySet();
    }

    private static FormDetails map(Obs obs, FormType formType) {

        Encounter encounter = obs.getEncounter();
        Visit visit = encounter.getVisit();
        User creator = obs.getCreator();

        FormDetails formDetails = new FormDetails();

        formDetails.setFormType(formType.getType());
        if (formType.equals(FormType.FORMS2)) {
            formDetails.setFormName(FormUtil.getFormNameFromFieldPath(obs.getFormFieldPath()));
            formDetails.setFormVersion(FormUtil.getFormVersionFromFieldPath(obs.getFormFieldPath()));
        } else if (formType.equals(FormType.FORMS1)) {
            formDetails.setFormName(obs.getConcept().getName().getName());
        }
        formDetails.setEncounterUuid(encounter.getUuid());
        formDetails.setEncounterDateTime(encounter.getEncounterDatetime());
        formDetails.setVisitUuid(visit.getUuid());
        formDetails.setVisitStartDateTime(visit.getStartDatetime());
        formDetails.addProvider(creator.getPersonName().getFullName(), creator.getUuid());
        return formDetails;
    }

    private static void addMultipleProvidersOfAForm(HashMap<FormDetails, FormDetails> formDetailsMap, FormDetails formDetails) {
        if (formDetailsMap.containsKey(formDetails)) {
            formDetails.getProviders().forEach(provider ->
                    formDetailsMap.get(formDetails).addProvider(provider.getProviderName(), provider.getUuid()));
        } else {
            formDetailsMap.put(formDetails, formDetails);
        }
    }

}
