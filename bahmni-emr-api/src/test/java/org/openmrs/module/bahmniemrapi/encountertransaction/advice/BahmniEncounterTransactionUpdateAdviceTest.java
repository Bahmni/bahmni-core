package org.openmrs.module.bahmniemrapi.encountertransaction.advice;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.powermock.api.mockito.PowerMockito.when;

import org.apache.commons.lang.StringUtils;
;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Context.class, OpenmrsUtil.class})
public class BahmniEncounterTransactionUpdateAdviceTest {
    private static String DEFAULT_ENCOUNTER_UUID = "defaultEncounterUuid";
    private static final String BAHMNI_EXECUTE_GROOVY_SCRIPT = "bahmni.executeGroovyObsValueCalculator" ;

    @Mock
    private AdministrationService administrationService;

    @Test
    public void shouldExecuteObsValueCalculatorFromApplicationDataDirectory() throws Throwable {
        PowerMockito.mockStatic(OpenmrsUtil.class);
        when(OpenmrsUtil.getApplicationDataDirectory()).thenReturn(getClass().getClassLoader().getResource("").getPath());
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(equalTo(DEFAULT_ENCOUNTER_UUID)));
    }

    @Test
    public void shouldLoadpplicationDataDirectoryPath() throws Throwable {
        PowerMockito.mockStatic(OpenmrsUtil.class);
        String path = getClass().getClassLoader().getResource("").getPath();
        // remove the trailing "/"
        path = StringUtils.chop(path);
        System.out.println(path);
        when(OpenmrsUtil.getApplicationDataDirectory()).thenReturn(path);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(equalTo(DEFAULT_ENCOUNTER_UUID)));
    }

    @Test
    public void shouldNotFailIfobscalculatorDirectoryDoesNotExist() throws Throwable {
        PowerMockito.mockStatic(OpenmrsUtil.class);
        when(OpenmrsUtil.getApplicationDataDirectory()).thenReturn(getClass().getClassLoader().getResource("").getPath() + "nonExistentDirectory");
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(BAHMNI_EXECUTE_GROOVY_SCRIPT)).thenReturn("true");

        BahmniEncounterTransaction bahmniEncounterTransaction = new BahmniEncounterTransaction();
        new BahmniEncounterTransactionUpdateAdvice().before(null, new BahmniEncounterTransaction[]{bahmniEncounterTransaction}, null);

        assertThat(bahmniEncounterTransaction.getEncounterUuid(), is(not(equalTo(DEFAULT_ENCOUNTER_UUID))));
    }
}
