package org.bahmni.module.bahmnicore.events.eventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.events.BahmniEventType;
import org.bahmni.module.bahmnicore.events.PatientEvent;
import org.bahmni.module.communication.service.CommunicationService;
import org.bahmni.module.communication.service.MessageBuilderService;
import org.openmrs.Patient;
import org.openmrs.PersonAttribute;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class PatientSmsEventListener {

    private final Log log = LogFactory.getLog(this.getClass());


    @Async("BahmniAsyncThreadExecutor")
    @EventListener
    public void onApplicationEvent(PatientEvent event) {
        try {
            Context.openSession();
            Context.setUserContext(event.userContext);
            if (event.eventType == BahmniEventType.BAHMNI_PATIENT_CREATED) {
                handlePatientCreatedEvent(event.getPatient());
            }
            } catch(Exception e){
                log.error("Exception occurred during event processing", e);
            } finally{
                Context.closeSession();
            }
    }

    private void handlePatientCreatedEvent(Patient patient) {
        AdministrationService administrationService = Context.getService(AdministrationService.class);
        boolean patientRegistrationSMSProperty = Boolean.parseBoolean(administrationService.getGlobalProperty("sms.enableRegistrationSMSAlert","false"));
        if (!patientRegistrationSMSProperty)
            return;
        String phoneNumber = getPhoneNumber(patient);
        if (phoneNumber != null) {
            MessageBuilderService messageBuilderService = Context.getRegisteredComponent("messageBuilderService", MessageBuilderService.class);
            CommunicationService communicationService = Context.getRegisteredComponent("communicationService", CommunicationService.class);
            String message=messageBuilderService.getRegistrationMessage(createArgumentsMapForPatientRegistration(patient));
            communicationService.sendSMS(phoneNumber, message);
        }
    }

    public Map<String, String> createArgumentsMapForPatientRegistration(Patient patient) {
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        Map<String, String> arguments = new HashMap<>();
        arguments.put("location", Context.getUserContext().getLocation().getName());
        arguments.put("identifier", patient.getPatientIdentifier().getIdentifier());
        arguments.put("patientname", patient.getGivenName() + " " + patient.getFamilyName());
        arguments.put("gender", patient.getGender());
        arguments.put("age", patient.getAge().toString());
        arguments.put("helpdesknumber", helpdeskNumber);
        return arguments;
    }

    private String getPhoneNumber(Patient patient) {
        PersonAttribute phoneNumber = patient.getAttribute("phoneNumber");
        if (phoneNumber == null) {
            log.info("No mobile number found for the patient. SMS not sent.");
            return null;
        }
        return phoneNumber.getValue();
    }
}


