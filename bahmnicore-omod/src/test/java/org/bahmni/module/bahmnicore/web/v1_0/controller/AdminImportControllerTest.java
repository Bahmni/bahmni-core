package org.bahmni.module.bahmnicore.web.v1_0.controller;

import org.apache.commons.io.FileUtils;
import org.bahmni.fileimport.FileImporter;
import org.bahmni.module.admin.csv.persister.PatientPersister;
import org.bahmni.module.bahmnicore.security.PrivilegeConstants;
import org.hibernate.SessionFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.User;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

import static org.bahmni.module.bahmnicore.web.v1_0.controller.AdminImportController.FILE_NAME_DATE_PART_FORMAT;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class AdminImportControllerTest {

    private static final String TMP_UPLOAD_DIR = "/tmp/admin_import_test";
    private static final String MINIMAL_PATIENT_CSV = "Patient_Identifier,First_Name,Last_Name\nP001,Test,Patient\n";

    AdminImportController controller;

    @Mock
    PatientPersister patientPersister;

    @Mock
    AdministrationService administrationService;

    @Mock
    SessionFactory sessionFactory;

    @Mock
    FileImporter mockFileImporter;

    @Mock
    UserContext mockUserContext;

    @Mock
    User mockUser;

    @Before
    public void setUp() throws Exception {
        controller = new AdminImportController() {
            @Override
            protected FileImporter createFileImporter() {
                return mockFileImporter;
            }
        };
        ReflectionTestUtils.setField(controller, "patientPersister", patientPersister);
        ReflectionTestUtils.setField(controller, "administrationService", administrationService);
        ReflectionTestUtils.setField(controller, "sessionFactory", sessionFactory);

        PowerMockito.mockStatic(Context.class);
        when(Context.getUserContext()).thenReturn(mockUserContext);
        when(mockUserContext.getAuthenticatedUser()).thenReturn(mockUser);
        when(mockUser.getSystemId()).thenReturn("admin");
        when(mockUser.getId()).thenReturn(1);
        when(mockUser.getUserId()).thenReturn(1);
        when(Context.getAuthenticatedUser()).thenReturn(mockUser);
        when(mockUserContext.hasPrivilege(any())).thenReturn(true);

        when(administrationService.getGlobalProperty(AdminImportController.PARENT_DIRECTORY_UPLOADED_FILES_CONFIG))
                .thenReturn(TMP_UPLOAD_DIR);

        when(mockFileImporter.importCSV(any(), any(), any(), any(), any(), any(), anyBoolean(), anyInt()))
                .thenReturn(true);

        FileUtils.deleteDirectory(new File(TMP_UPLOAD_DIR));
    }

    @Test
    public void shouldReturn403WhenUserDoesNotHaveAdminPrivilege() throws Exception {
        when(mockUserContext.hasPrivilege(PrivilegeConstants.IMPORT_CSV_FILE_PRIVILEGE)).thenReturn(false);

        MockMultipartFile file = new MockMultipartFile("file", "patients.csv", "text/csv", new byte[0]);
        ResponseEntity<Serializable> response = controller.upload(file, "localhost", null, null);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    @Test
    public void shouldReturn200WhenUploadingPatientCSVWithAdminPrivilege() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "patients.csv", "text/csv", MINIMAL_PATIENT_CSV.getBytes());
        ResponseEntity<Serializable> response = controller.upload(file, "localhost", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void shouldStoreUploadedFileWithRandomNameNotOriginalFilename() throws Exception {
        String originalFileName = "../../etc/patients.csv";

        MockMultipartFile file = new MockMultipartFile("file", originalFileName, "text/csv", MINIMAL_PATIENT_CSV.getBytes());
        ResponseEntity<Serializable> response = controller.upload(file, "localhost", null, null);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertFalse("File should not be written at the path",
                new File(TMP_UPLOAD_DIR + "/patient/" + originalFileName).exists());

        File patientDir = new File(TMP_UPLOAD_DIR + "/patient/");
        assertTrue("patient upload directory should be created", patientDir.exists());
        String fileNameDatePart = new SimpleDateFormat(FILE_NAME_DATE_PART_FORMAT).format(new Date());
        File[] uploadedFiles = patientDir.listFiles((dir, name) -> name.startsWith("patient-"+fileNameDatePart) && name.endsWith(".csv"));
        assertTrue("a randomly named patient CSV should exist in the upload directory",
                uploadedFiles != null && uploadedFiles.length > 0);
    }
}
