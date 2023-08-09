package org.bahmni.module.bahmnicore.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.module.bahmnicore.contract.SMS.SMSRequest;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.bahmni.module.bahmnicore.service.BahmniDrugOrderService;
import org.bahmni.module.bahmnicore.service.SMSService;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Pattern;

@Service
public class SMSServiceImpl implements SMSService {
    private static Logger logger = LogManager.getLogger(BahmniDrugOrderService.class);
    private final static String REGISTRATION_SMS_TEMPLATE = "sms.registrationSMSTemplate";
    private final static String SMS_URI = "sms.uri";

    @Override
    public String sendSMS(String phoneNumber, String message) {
        try {
            SMSRequest smsRequest = new SMSRequest();
            smsRequest.setPhoneNumber(phoneNumber);
            smsRequest.setMessage(message);
            ObjectMapper objMapper = new ObjectMapper();
            String jsonObject = objMapper.writeValueAsString(smsRequest);
            StringEntity params = new StringEntity(jsonObject);


            String smsUrl = StringUtils.isBlank(BahmniCoreProperties.getProperty("sms.uri")) ? SMS_URI : BahmniCoreProperties.getProperty("sms.uri");
            HttpPost request = new HttpPost(Context.getMessageSourceService().getMessage(smsUrl, null, new Locale("en")));
            request.addHeader("content-type", "application/json");
            String smsPropertiesPath = BahmniCoreProperties.getProperty("sms.token.path");
            BufferedReader bufferedReader;
            try (FileReader reader = new FileReader(smsPropertiesPath)) {
                bufferedReader = new BufferedReader(reader);
                request.addHeader("Authorization", "Bearer " + bufferedReader.readLine());
            } catch (IOException e) {
                throw new RuntimeException("Error loading SMS properties file.", e);
            }
            request.setEntity(params);
            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpResponse response = httpClient.execute(request);
            httpClient.close();
            return response.getStatusLine().getReasonPhrase();
        } catch (Exception e) {
            logger.error("Exception occured in sending sms ", e);
            throw new RuntimeException("Exception occured in sending sms ", e);
        }
    }

    @Override
    public String getRegistrationMessage(Locale locale, Patient patient, Location location) {
        String helpdeskNumber = Context.getAdministrationService().getGlobalPropertyObject("clinic.helpDeskNumber").getPropertyValue();
        String clinicTime = Context.getAdministrationService().getGlobalPropertyObject("clinic.clinicTimings").getPropertyValue();

        Map<String, Object> arguments = createArgumentsMap(location, patient, helpdeskNumber, clinicTime);

        return templateMessage(REGISTRATION_SMS_TEMPLATE, arguments);
    }

    private Map<String, Object> createArgumentsMap(Location location, Patient patient, String helpdeskNumber, String clinicTime) {
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("location", location.getName());
        arguments.put("identifier", patient.getPatientIdentifier().getIdentifier());
        arguments.put("patientname", patient.getGivenName() + " " + patient.getFamilyName());
        arguments.put("gender", patient.getGender());
        arguments.put("age", patient.getAge().toString());
        arguments.put("helpdesknumber", helpdeskNumber);
        arguments.put("facilitytimings", clinicTime);
        return arguments;
    }

    public String templateMessage(String smsTemplate, Map<String, Object> arguments) {
        String template = Context.getAdministrationService().getGlobalProperty(smsTemplate);
        String formattedMessage = StringUtils.isBlank(template) ? Context.getMessageSourceService().getMessage(smsTemplate, null, new Locale("en")) : template;

        Pattern pattern = Pattern.compile("\\{([^}]*)\\}");
        Matcher matcher = pattern.matcher(formattedMessage);
        while (matcher.find()) {
            String placeholder = matcher.group(1);
            String modifiedPlaceholder = placeholder.toLowerCase().replaceAll("\\s", "");
            Object value = arguments.get(modifiedPlaceholder);
            placeholder = String.format("{%s}", placeholder);
            formattedMessage = formattedMessage.replace(placeholder, String.valueOf(value));
        }

        return formattedMessage.replace("\\n", System.lineSeparator());
    }

}