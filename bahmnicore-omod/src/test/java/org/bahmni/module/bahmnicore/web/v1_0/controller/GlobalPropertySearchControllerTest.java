package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.openmrs.GlobalProperty;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class GlobalPropertySearchControllerTest {

    @Mock
    private AdministrationService administrationService;

    @InjectMocks
    private GlobalPropertySearchController globalPropertySearchController;

    private List<GlobalProperty> globalPropertyList = new ArrayList<GlobalProperty>();


    @Test
    public void shouldReturnPasswordRelatedPolicies() throws Exception {
        GlobalProperty passwordMinLength = new GlobalProperty("security.passwordMinLength","8");
        GlobalProperty globalProperty = new GlobalProperty("gender","F, M");
        GlobalProperty passwordCantMatchUserName = new GlobalProperty("security.passwordShouldNotMatchUserName","true");

        globalPropertyList.add(passwordMinLength);
        globalPropertyList.add(globalProperty);
        globalPropertyList.add(passwordCantMatchUserName);

        when(administrationService.getAllGlobalProperties()).thenReturn(globalPropertyList);

        ResponseEntity<HashMap<String, String>> passwordPolicies = globalPropertySearchController.getPasswordPolicies();

        assertEquals(2, passwordPolicies.getBody().size());
        assertEquals("8", passwordPolicies.getBody().get("security.passwordMinLength"));
        assertEquals("true", passwordPolicies.getBody().get("security.passwordShouldNotMatchUserName"));
    }

    @Test
    public void shouldReturnEmptyListIfPasswordPoliciesAreNotThere() throws Exception {
        GlobalProperty globalProperty = new GlobalProperty("gender","F, M");
        globalPropertyList.add(globalProperty);

        when(administrationService.getAllGlobalProperties()).thenReturn(globalPropertyList);

        ResponseEntity<HashMap<String, String>> passwordPolicies = globalPropertySearchController.getPasswordPolicies();

        assertEquals(0, passwordPolicies.getBody().size());

    }
}