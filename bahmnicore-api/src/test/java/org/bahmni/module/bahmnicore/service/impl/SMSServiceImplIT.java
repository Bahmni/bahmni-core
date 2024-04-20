package org.bahmni.module.bahmnicore.service.impl;

import org.bahmni.test.builder.PersonBuilder;
import org.bahmni.test.builder.VisitBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.Location;
import org.openmrs.Visit;
import org.openmrs.Person;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
@PowerMockIgnore("javax.management.*")
public class SMSServiceImplIT {

    private SMSServiceImpl smsService;
    @Mock
    AdministrationService administrationService;
    @Mock
    MessageSourceService messageSourceService;
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        smsService = new SMSServiceImpl();
        initMocks(this);
        mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(Context.getMessageSourceService()).thenReturn(messageSourceService);
    }

    @Test
    public void shouldReturnPrescriptionSMSWithGlobalPropertyTemplate() throws ParseException {
        when(administrationService.getGlobalProperty("bahmni.prescriptionSMSTemplate")).thenReturn("Date: {0}\nPrescription For Patient: {1}, {2}, {3} years.\nDoctor: {4} ({5})\n{6}");
        when(messageSourceService.getMessage("bahmni.sms.timezone", null, new Locale("en"))).thenReturn("IST");
        when(messageSourceService.getMessage("bahmni.sms.dateformat", null, new Locale("en"))).thenReturn("dd-MM-yyyy");
        Date visitDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2023");
        Date birthDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2010");
        Person person = new PersonBuilder().withUUID("puuid").withPersonName("testPersonName").build();
        person.setGender("M");
        person.setBirthdate(birthDate);
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).build();
        Location location = new Location();
        location.setName("Bahmni");

        String prescriptionContent = smsService.getPrescriptionMessage(new Locale("en"), visit.getStartDatetime(), visit.getPatient(), location, Arrays.asList("Superman"), "1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023");
        String expectedPrescriptionContent = "Date: 30-01-2023\n" +
                "Prescription For Patient: testPersonName null, M, 13 years.\n" +
                "Doctor: Superman (Bahmni)\n" +
                "1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023";
        assertEquals(expectedPrescriptionContent, prescriptionContent);
    }

    @Test
    public void shouldReturnPrescriptionSMSWithDefaultTemplate() throws ParseException {
        Object[] args = {"30-01-2023", "testPersonName null", "M", "13", "Superman", "Bahmni", "1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023"};
        when(administrationService.getGlobalProperty("bahmni.prescriptionSMSTemplate")).thenReturn(null);
        when(messageSourceService.getMessage("bahmni.prescriptionSMSTemplate", args, new Locale("en"))).thenReturn("Date: 30-01-2023\nPrescription For Patient: testPersonName null, M, 13 years.\nDoctor: Superman (Bahmni)\n1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023");
        when(messageSourceService.getMessage("bahmni.sms.timezone", null, new Locale("en"))).thenReturn("IST");
        when(messageSourceService.getMessage("bahmni.sms.dateformat", null, new Locale("en"))).thenReturn("dd-MM-yyyy");
        Date visitDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2023");
        Date birthDate = new SimpleDateFormat("MMMM d, yyyy", new Locale("en")).parse("January 30, 2010");
        Person person = new PersonBuilder().withUUID("puuid").withPersonName("testPersonName").build();
        person.setGender("M");
        person.setBirthdate(birthDate);
        Visit visit = new VisitBuilder().withPerson(person).withUUID("vuuid").withStartDatetime(visitDate).build();
        Location location = new Location();
        location.setName("Bahmni");

        String prescriptionContent = smsService.getPrescriptionMessage(new Locale("en"), visit.getStartDatetime(), visit.getPatient(), location, Arrays.asList("Superman"), "1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023");
        String expectedPrescriptionContent = "Date: 30-01-2023\n" +
                "Prescription For Patient: testPersonName null, M, 13 years.\n" +
                "Doctor: Superman (Bahmni)\n" +
                "1. Paracetamol 150 mg/ml, 50 ml, Immediately-1 Days, start from 31-01-2023";
        assertEquals(expectedPrescriptionContent, prescriptionContent);
    }

    @Test
    public void shouldThrowNullPointerExceptionOnNullUrl() throws Exception {
        when(messageSourceService.getMessage("bahmni.sms.url", null, new Locale("en"))).thenReturn(null);
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Exception occured in sending sms");
        expectedEx.expectCause(instanceOf(java.lang.NullPointerException.class));
        smsService.sendSMS("+919999999999", "Welcome");
    }

    @Test
    public void shouldNotThrowNullPointerExceptionOnValidUrl() throws Exception {
        when(messageSourceService.getMessage("bahmni.sms.url", null, new Locale("en"))).thenReturn("http://google.com");
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Exception occured in sending sms");
        expectedEx.isAnyExceptionExpected();
        expectedEx.expectCause(not(instanceOf(java.lang.NullPointerException.class)));
        smsService.sendSMS("+919999999999", "Welcome");
    }

}
