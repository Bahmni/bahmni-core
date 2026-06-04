package org.openmrs.module.bahmniemrapi.encountertransaction.command.impl;

import org.joda.time.DateTime;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.bahmniemrapi.encountertransaction.command.EncounterDataPreSaveCommand;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class OrderSaveCommandImpl implements EncounterDataPreSaveCommand {

    private AdministrationService adminService;
    public static final int DEFAULT_SESSION_DURATION_IN_MINUTES = 60;

    @Autowired
    public OrderSaveCommandImpl(@Qualifier("adminService") AdministrationService administrationService) {
        this.adminService = administrationService;
    }

    @Override
    public BahmniEncounterTransaction update(BahmniEncounterTransaction bahmniEncounterTransaction) {
        String configuredSessionDuration = adminService.getGlobalProperty("bahmni.encountersession.duration");
        int encounterSessionDuration = configuredSessionDuration != null ? Integer.parseInt(configuredSessionDuration) : DEFAULT_SESSION_DURATION_IN_MINUTES;

        for (EncounterTransaction.Order order : bahmniEncounterTransaction.getOrders()) {
            if(order.getAutoExpireDate() == null){
                order.setAutoExpireDate(DateTime.now().plusMinutes(encounterSessionDuration).toDate());
            }
        }
        return bahmniEncounterTransaction;
    }
}
