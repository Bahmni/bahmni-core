package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
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
    public Object sendEmail(@RequestBody MailContent mailContent) {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = null;
        try {
            Context.getService(CommunicationService.class).sendEmail(mailContent);
            response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, null), null);
        } catch (Exception exception) {
            log.error("Unable to send email", exception);
            response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to send email"), null);
        }
        return response;
    }
}