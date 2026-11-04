package org.openmrs.module.bahmniemrapi.encountertransaction.advice;

import org.junit.After;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.util.OpenmrsUtil;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import java.io.File;

public class BahmniEncounterTransactionUpdateAdviceTest {
    private static String DEFAULT_ENCOUNTER_UUID = "defaultEncounterUuid";
    private static final String BAHMNI_EXECUTE_GROOVY_SCRIPT = "bahmni.executeGroovyObsValueCalculator" ;

    @Mock
    private AdministrationService administrationService;

    private MockedStatic<OpenmrsUtil> openmrsUtilMockedStatic;
    private MockedStatic<Context> contextMockedStatic;

    @After
    public void tearDown() {
        if (openmrsUtilMockedStatic != null) {
            openmrsUtilMockedStatic.close();
        }
        if (contextMockedStatic != null) {
            contextMockedStatic.close();
        }
    }

    private String getTestClassesDir() throws Exception {
        return new File(getClass().getClassLoader().getResource("").toURI()).getAbsolutePath();
    }

    @Test
    public void shouldExecuteObsValueCalculatorFromApplicationDataDirectory() throws Throwable {
        initMocks(this);
        openmrsUtilMockedStatic = Mockito.mockStatic(OpenmrsUtil.class);
        openmrsUtilMockedStatic.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(getTestClassesDir() + File.separator);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(equalTo(DEFAULT_ENCOUNTER_UUID)));
    }

    @Test
    public void shouldLoadpplicationDataDirectoryPath() throws Throwable {
        initMocks(this);
        openmrsUtilMockedStatic = Mockito.mockStatic(OpenmrsUtil.class);
        // no trailing "/" on this path, unlike the other test
        String path = getTestClassesDir();
        System.out.println(path);
        openmrsUtilMockedStatic.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(path);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(equalTo(DEFAULT_ENCOUNTER_UUID)));
    }

    @Test
    public void shouldNotFailIfobscalculatorDirectoryDoesNotExist() throws Throwable {
        initMocks(this);
        openmrsUtilMockedStatic = Mockito.mockStatic(OpenmrsUtil.class);
        openmrsUtilMockedStatic.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(getTestClassesDir() + File.separator + "nonExistentDirectory");
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(not(equalTo(DEFAULT_ENCOUNTER_UUID))));
    }
}
