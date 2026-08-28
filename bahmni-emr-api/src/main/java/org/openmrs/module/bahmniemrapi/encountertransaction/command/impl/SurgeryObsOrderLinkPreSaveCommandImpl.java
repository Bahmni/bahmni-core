package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniObservation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class SurgeryObsOrderLinkPreSaveCommandImpl implements EncounterDataPreSaveCommand {

    static final String SURGERY_SELECTION_CONCEPT_UUID_GP = "bahmnicore.order.surgerySelectionConceptUuid";

    private final AdministrationService adminService;

    @Autowired
    public SurgeryObsOrderLinkPreSaveCommandImpl(@Qualifier("adminService") AdministrationService adminService) {
        this.adminService = adminService;
    }

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        String surgerySelectionConceptUuid = adminService.getGlobalProperty(SURGERY_SELECTION_CONCEPT_UUID_GP, "");
        if (StringUtils.isBlank(surgerySelectionConceptUuid)) {
            return bahmniEncounterTransaction;
        }

        Map<String, List<BahmniObservation>> obsByForm = groupObsByForm(bahmniEncounterTransaction.getObservations());
        for (List<BahmniObservation> formObs : obsByForm.values()) {
            String orderUuid = findOrderUuid(formObs, surgerySelectionConceptUuid);
            if (StringUtils.isNotBlank(orderUuid)) {
                setOrderUuidOnObs(formObs, orderUuid);
            }
        }

        return bahmniEncounterTransaction;
    }

    private Map<String, List<BahmniObservation>> groupObsByForm(Collection<BahmniObservation> observations) {
        Map<String, List<BahmniObservation>> result = new LinkedHashMap<>();
        for (BahmniObservation obs : observations) {
            result.computeIfAbsent(extractFormName(obs.getFormFieldPath()), k -> new ArrayList<>()).add(obs);
        }
        return result;
    }

    private String extractFormName(String formFieldPath) {
        if (StringUtils.isBlank(formFieldPath)) {
            return "";
        }
        int slash = formFieldPath.indexOf('/');
        return slash >= 0 ? formFieldPath.substring(0, slash) : formFieldPath;
    }

    private String findOrderUuid(Collection<BahmniObservation> observations, String surgerySelectionConceptUuid) {
        for (BahmniObservation obs : observations) {
            if (!obs.getVoided() && surgerySelectionConceptUuid.equals(obs.getConceptUuid())) {
                Object value = obs.getValue();
                if (value != null && StringUtils.isNotBlank(value.toString())) {
                    return value.toString();
                }
            }
            String found = findOrderUuid(obs.getGroupMembers(), surgerySelectionConceptUuid);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private void setOrderUuidOnObs(Collection<BahmniObservation> observations, String orderUuid) {
        for (BahmniObservation obs : observations) {
            if (!obs.getVoided() && StringUtils.isBlank(obs.getOrderUuid())) {
                obs.setOrderUuid(orderUuid);
            }
            setOrderUuidOnObs(obs.getGroupMembers(), orderUuid);
        }
    }

}
