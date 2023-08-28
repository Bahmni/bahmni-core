package org.bahmni.module.bahmnicore.eventListener;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.events.api.model.BahmniEventType;
import org.openmrs.Person;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.bahmni.module.events.api.model.Event;

import java.util.Locale;

@Component
public class SMSEventListenerBahmniCore {


    private final Log log = LogFactory.getLog(this.getClass());


    @EventListener
    public void onApplicationEvent(Event event) {
        System.out.println("inside event listner bahmni core");
        if (event.eventType == BahmniEventType.BAHMNI_PATIENT_CREATED) {
            Boolean isregistrationSmsEnabled = Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("sms.enableRegistrationSMSAlert"));
            if (isregistrationSmsEnabled) {
                System.out.println("hi");
                Object person = ((SimpleObject) event.payload).get("person");
//            String phoneNumber = person.get("phoneNumebr");
//            if (null == phoneNumber) {
//                log.info("Since no mobile number found for the patient. SMS not sent.");
//                return;
//            }
//            if (savePatientEvent.isRegistrationSmsEnabled()) {
//            UserContext userContext = savePatientEvent.getUserContext();
//            Context.openSession();
//            Context.setUserContext(userContext);

//            Location location = Context.getLocationService().getLocationByUuid(locationUuid);
//            String message = smsService.getRegistrationMessage(new Locale("en"), person, location);
//            smsService.sendSMS(phoneNumber, message);
//            }
            }
        }
    }
}
