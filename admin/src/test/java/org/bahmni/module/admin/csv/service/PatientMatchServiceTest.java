package org.bahmni.module.admin.csv.service;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;

public class PatientMatchServiceTest {

    private static final String APP_DATA_DIR_PROPERTY = "OPENMRS_APPLICATION_DATA_DIRECTORY";
    private static final String ALGORITHM_DIR_NAME = "patientMatchingAlgorithm";

    @Rule
    public TemporaryFolder appDataDir = new TemporaryFolder();

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    private String originalAppDataDirProperty;
    private Method getAlgorithmClassPath;

    @Before
    public void setUp() throws Exception {
        originalAppDataDirProperty = System.getProperty(APP_DATA_DIR_PROPERTY);
        System.setProperty(APP_DATA_DIR_PROPERTY, appDataDir.getRoot().getAbsolutePath());
        appDataDir.newFolder(ALGORITHM_DIR_NAME);

        getAlgorithmClassPath = PatientMatchService.class.getDeclaredMethod("getAlgorithmClassPath", String.class);
        getAlgorithmClassPath.setAccessible(true);
    }

    @After
    public void tearDown() {
        if (originalAppDataDirProperty == null) {
            System.clearProperty(APP_DATA_DIR_PROPERTY);
        } else {
            System.setProperty(APP_DATA_DIR_PROPERTY, originalAppDataDirProperty);
        }
    }

    @Test
    public void shouldResolveASimpleNameInsideTheAlgorithmDirectory() throws Exception {
        String resolvedPath = invoke("SimpleAlgorithm.groovy");
        String expectedPath = new File(new File(appDataDir.getRoot(), ALGORITHM_DIR_NAME), "SimpleAlgorithm.groovy").getPath();
        assertEquals(expectedPath, resolvedPath);
    }

    @Test
    public void shouldRejectRelativePathTraversalOutsideTheAlgorithmDirectory() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid patientMatchingAlgorithm");
        invoke("../../../../tmp/evil/Pwn.groovy");
    }

    @Test
    public void shouldRejectAbsolutePathPayload() throws Exception {
        expectedEx.expect(IllegalArgumentException.class);
        expectedEx.expectMessage("Invalid patientMatchingAlgorithm");
        invoke("/etc/passwd");
    }

    private String invoke(String matchingAlgorithmClassName) throws Exception {
        try {
            return (String) getAlgorithmClassPath.invoke(new PatientMatchService(), matchingAlgorithmClassName);
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof RuntimeException) {
                throw (RuntimeException) e.getCause();
            }
            throw e;
        }
    }
}
