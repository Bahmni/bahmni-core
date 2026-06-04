package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseFactory;
import org.apache.http.HttpStatus;
import org.apache.http.HttpVersion;
import org.apache.http.impl.DefaultHttpResponseFactory;
import org.apache.http.message.BasicStatusLine;
import org.bahmni.module.bahmnicore.web.v1_0.contract.BahmniMailContent;
import org.bahmni.module.communication.model.Recipient;
import org.bahmni.module.communication.service.CommunicationService;
import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequestMapping(value = "/rest/" + RestConstants.VERSION_1 + "/patient/{patientUuid}/send/")//change to send-prescription-email
public class EmailCommunicationController extends BaseRestController {

    private final Log log = LogFactory.getLog(this.getClass());

    @Autowired
    PatientService patientService;

    @PostMapping(value = "email")
    @ResponseBody
    public Object sendEmail(@RequestBody BahmniMailContent bahmniMailContent, @PathVariable("patientUuid") String patientUuid) {
        HttpResponseFactory factory = new DefaultHttpResponseFactory();
        HttpResponse response = null;
        if(Boolean.valueOf(Context.getAdministrationService().getGlobalProperty("bahmni.enableEmailPrescriptionOption"))) {
            try {
                Patient patient = patientService.getPatientByUuid(patientUuid);
                String recipientName = patient.getGivenName() + (patient.getMiddleName()!=null ? " " + patient.getMiddleName() : "") + (patient.getFamilyName()!=null ? " " + patient.getFamilyName() : "");
                String recipientEmail = patient.getAttribute("email").getValue();
                Recipient recipient = new Recipient(recipientName, recipientEmail);
                bahmniMailContent.setRecipient(recipient);
                Context.getService(CommunicationService.class).sendEmail(bahmniMailContent);
                response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_OK, "Mail sent successfully"), null);
            } catch (Exception exception) {
                log.error("Unable to send email", exception);
                response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_INTERNAL_SERVER_ERROR, "Unable to send email"), null);
            }
        } else {
            response = factory.newHttpResponse(new BasicStatusLine(HttpVersion.HTTP_1_1, HttpStatus.SC_METHOD_NOT_ALLOWED, "Sending email is not enabled"), null);
        }
        return response;
    }

}