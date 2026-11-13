package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniEncounterModifierServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private AdministrationService administrationService;

    private File appDataDir;
    private BahmniEncounterModifierServiceImpl service;

    private MockedStatic<OpenmrsUtil> openmrsUtilMockedStatic;
    private MockedStatic<Context> contextMockedStatic;

    @After
    public void tearDown() {
        openmrsUtilMockedStatic.close();
        contextMockedStatic.close();
    }

    private void setUp(String allowCaching) throws IOException {
        initMocks(this);

        appDataDir = temporaryFolder.newFolder("openmrsAppData");
        new File(appDataDir, "encounterModifier").mkdirs();

        openmrsUtilMockedStatic = Mockito.mockStatic(OpenmrsUtil.class);
        openmrsUtilMockedStatic.when(OpenmrsUtil::getApplicationDataDirectory).thenReturn(appDataDir.getAbsolutePath());

        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
        when(administrationService.getGlobalProperty("encounterModifier.groovy.allowCaching")).thenReturn(allowCaching);

        service = new BahmniEncounterModifierServiceImpl();
    }

    @Test
    public void shouldLoadEncounterModifierThatIsInsideTheConfiguredDirectory() throws Throwable {
        setUp("false");

        File legit = new File(appDataDir, "encounterModifier/DiabetesPanel.groovy");
        FileUtils.writeStringToFile(legit,
                "import org.bahmni.module.bahmnicore.encounterModifier.EncounterModifier\n" +
                "import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData\n" +
                "class DiabetesPanel extends EncounterModifier {\n" +
                "    EncounterModifierData run(EncounterModifierData d) { return d }\n" +
                "}\n");

        EncounterModifierData request = requestFor("Diabetes Panel");

        EncounterModifierData result = service.getModifiedEncounter(request);

        assertNotEquals(null, result);
    }

    @Test
    public void shouldRejectConceptSetNameThatTraversesOutsideTheConfiguredDirectory() throws Throwable {
        setUp("false");

        File outsideDir = temporaryFolder.newFolder("someOtherDirectory");
        File canary = new File(outsideDir, "Pwn.groovy");
        FileUtils.writeStringToFile(canary,
                "import org.bahmni.module.bahmnicore.encounterModifier.EncounterModifier\n" +
                "import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData\n" +
                "class Pwn extends EncounterModifier {\n" +
                "    static {\n" +
                "        System.setProperty('poc.pwned', 'true')\n" +
                "    }\n" +
                "    EncounterModifierData run(EncounterModifierData d) { return d }\n" +
                "}\n");
        System.clearProperty("poc.pwned");

        EncounterModifierData request = requestFor("../../someOtherDirectory/Pwn");

        try {
            service.getModifiedEncounter(request);
            fail("Expected an IOException rejecting the path traversal attempt");
        } catch (IOException e) {
            // expected
        }

        assertNull("Groovy file outside /encounterModifier/ must never be loaded/executed", System.getProperty("poc.pwned"));
    }

    private EncounterModifierData requestFor(String conceptSetName) {
        ConceptData conceptSetData = new ConceptData();
        conceptSetData.setName(conceptSetName);

        EncounterModifierData request = new EncounterModifierData();
        request.setConceptSetData(conceptSetData);
        return request;
    }
}
