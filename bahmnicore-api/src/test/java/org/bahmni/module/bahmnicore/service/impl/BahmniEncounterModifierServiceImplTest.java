package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.bahmni.module.bahmnicore.contract.encounter.data.ConceptData;
import org.bahmni.module.bahmnicore.contract.encounter.data.EncounterModifierData;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.util.OpenmrsUtil;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.io.IOException;

import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class, OpenmrsUtil.class})
public class BahmniEncounterModifierServiceImplTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private AdministrationService administrationService;

    private File appDataDir;
    private BahmniEncounterModifierServiceImpl service;

    private void setUp(String allowCaching) throws IOException {
        initMocks(this);

        appDataDir = temporaryFolder.newFolder("openmrsAppData");
        new File(appDataDir, "encounterModifier").mkdirs();

        PowerMockito.mockStatic(OpenmrsUtil.class);
        when(OpenmrsUtil.getApplicationDataDirectory()).thenReturn(appDataDir.getAbsolutePath());

        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
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
