package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.communication.api.CommunicationService;
import org.bahmni.module.communication.model.MailContent;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import javax.mail.MessagingException;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/send/")
public class TransmissionController {

    private final Log log = LogFactory.getLog(this.getClass());

    @PostMapping(value = "email")
    @ResponseBody
    public void sendEmail(@RequestBody MailContent mailContent) {
        try {
            Context.getService(CommunicationService.class).sendEmail(mailContent);
        } catch (MessagingException exception) {
            log.error("Unable to send email", exception);
        }
    }
}