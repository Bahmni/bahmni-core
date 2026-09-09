package org.bahmni.module.bahmnicore.extensions;

import org.apache.commons.io.FileUtils;
import org.bahmni.module.bahmnicore.dao.ApplicationDataDirectory;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.when;

public class BahmniExtensionsTest {

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    private ApplicationDataDirectory applicationDataDirectory;

    private BahmniExtensions bahmniExtensions;
    private File extensionDir;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        bahmniExtensions = new BahmniExtensions();
        ReflectionTestUtils.setField(bahmniExtensions, "applicationDataDirectory", applicationDataDirectory);

        extensionDir = temporaryFolder.newFolder("openmrs", "flowsheetExtension");
        when(applicationDataDirectory.getFileFromConfig("openmrs" + File.separator + "flowsheetExtension"))
                .thenReturn(extensionDir);
    }

    @Test
    public void shouldLoadExtensionThatIsInsideTheConfiguredDirectory() throws Exception {
        File groovyFile = new File(extensionDir, "MyExtension.groovy");
        FileUtils.writeStringToFile(groovyFile, "class MyExtension { }");
        when(applicationDataDirectory.getFileFromConfig(
                "openmrs" + File.separator + "flowsheetExtension" + File.separator + "MyExtension.groovy"))
                .thenReturn(groovyFile);

        Object extension = bahmniExtensions.getExtension("flowsheetExtension", "MyExtension.groovy");

        assertNotNull(extension);
    }

    @Test
    public void shouldRejectPathTraversalAttemptOutsideConfiguredDirectory() throws Exception {
        File outsideMarker = new File(temporaryFolder.getRoot(), "Pwn.groovy");
        FileUtils.writeStringToFile(outsideMarker,
                "class Pwn { public Pwn() { throw new RuntimeException(\"pwned\") } }");
        String traversalFileName = ".." + File.separator + ".." + File.separator + "Pwn.groovy";
        when(applicationDataDirectory.getFileFromConfig(
                "openmrs" + File.separator + "flowsheetExtension" + File.separator + traversalFileName))
                .thenReturn(new File(extensionDir, traversalFileName));

        Object extension = bahmniExtensions.getExtension("flowsheetExtension", traversalFileName);

        assertNull(extension);
    }

    @Test
    public void shouldReturnNullWithoutThrowingWhenFileDoesNotExist() throws Exception {
        when(applicationDataDirectory.getFileFromConfig(
                "openmrs" + File.separator + "flowsheetExtension" + File.separator + "DoesNotExist.groovy"))
                .thenReturn(new File(extensionDir, "DoesNotExist.groovy"));

        Object extension = bahmniExtensions.getExtension("flowsheetExtension", "DoesNotExist.groovy");

        assertNull(extension);
    }

    @Test
    public void shouldReturnNullWhenFileNameIsBlank() {
        assertNull(bahmniExtensions.getExtension("flowsheetExtension", null));
        assertNull(bahmniExtensions.getExtension("flowsheetExtension", ""));
        assertNull(bahmniExtensions.getExtension("flowsheetExtension", "   "));
    }
}
