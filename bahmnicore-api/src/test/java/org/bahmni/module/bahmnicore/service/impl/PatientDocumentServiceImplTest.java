package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.bahmni.module.bahmnicore.BahmniCoreException;
import org.bahmni.module.bahmnicore.bahmniexceptions.FileTypeNotSupportedException;
import org.bahmni.module.bahmnicore.bahmniexceptions.VideoFormatNotSupportedException;
import org.bahmni.module.bahmnicore.model.VideoFormats;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.bahmni.module.bahmnicore.service.ThumbnailGenerator;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.openmrs.Patient;
import org.openmrs.api.context.UserContext;
import org.springframework.http.ResponseEntity;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class PatientDocumentServiceImplTest {

    private PatientDocumentServiceImpl patientDocumentService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    public TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    ThumbnailGenerator thumbnailGenerator;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    private MockedStatic<Context> contextMockedStatic;

    @After
    public void tearDown() {
        contextMockedStatic.close();
    }

    @Before
    public void setUp() {
        initMocks(this);
        contextMockedStatic = Mockito.mockStatic(Context.class);
        contextMockedStatic.when(Context::getAdministrationService).thenReturn(administrationService);
    }

    @Test
    public void shouldCreateRightDirectoryAccordingToPatientId() {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");

            patientDocumentService = new PatientDocumentServiceImpl();

            String url = patientDocumentService.createFilePath(".", 280, "Radiology", "jpeg", "file-name");

            assertFalse(url.isEmpty());
            assertTrue(url.startsWith("300/280-Radiology-"));
            assertTrue(url.endsWith(".jpeg"));

            File absoluteFileDirectory = new File("./300");
            absoluteFileDirectory.delete();
        }
    }

    @Test
    public void shouldReturn404WhenPathTraversalAttemptedViaPatientUuidOnV2() throws Exception {
        File imagesDirectory = temporaryFolder.newFolder("patient_images");
        temporaryFolder.newFile("secret.jpeg");
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn(imagesDirectory.getAbsolutePath());
            patientDocumentService = new PatientDocumentServiceImpl();

            ResponseEntity<Object> responseEntity = patientDocumentService.retriveImageWithoutDefault("../secret");

            assertEquals(404, responseEntity.getStatusCode().value());
        }
    }

    @Test
    public void shouldReturn404WhenValidPatientUuidButNoImageExists() {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
            patientDocumentService = new PatientDocumentServiceImpl();

            ResponseEntity<Object> responseEntity = patientDocumentService.retriveImageWithoutDefault("valid-patient-uuid");

            assertEquals(404, responseEntity.getStatusCode().value());
        }
    }

    @Test
    public void shouldReturnImageWhenPatientImageExists() throws Exception {
        File tempFile = temporaryFolder.newFile("patient-uuid.jpeg");
        Files.write(tempFile.toPath(), new byte[]{1, 2, 3});
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
            patientDocumentService = new PatientDocumentServiceImpl();

            ResponseEntity<Object> responseEntity = patientDocumentService.retriveImage("patient-uuid");

            assertEquals(200, responseEntity.getStatusCode().value());
        }
    }

    @Test
    public void shouldNotSaveImageWhenImageIsNull() {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn("/bahmni_data/patient_images");
            patientDocumentService = new PatientDocumentServiceImpl();

            patientDocumentService.saveImage("patient-uuid", null);
        }
    }

    @Test
    public void shouldThrowExceptionWhenBlankFileNameOnDelete() {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("/bahmni_data");
            patientDocumentService = new PatientDocumentServiceImpl();

            try {
                patientDocumentService.delete("");
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Required String parameter"));
            }
        }
    }

    @Test
    public void shouldThrowExceptionWhenPathTraversalAttemptedOnDelete() {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("/bahmni_data");
            patientDocumentService = new PatientDocumentServiceImpl();

            try {
                patientDocumentService.delete("../../etc/passwd.jpeg");
                fail("Expected RuntimeException");
            } catch (RuntimeException e) {
                assertTrue(e.getMessage().contains("Invalid file specified"));
            }
        }
    }

    @Test
    public void shouldDeleteFileSuccessfully() throws Exception {
        File tempFile = temporaryFolder.newFile("patient-doc.jpeg");
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());
            patientDocumentService = new PatientDocumentServiceImpl();

            patientDocumentService.delete("patient-doc.jpeg");

            assertFalse(tempFile.exists());
        }
    }

    @Test
    public void shouldGetImageNotFoundForIfNoImageCapturedForPatientAndNoDefaultImageNotPresent() throws Exception {
        // Note: the original PowerMock version of this test stubbed
        // whenNew(FileInputStream.class).withArguments(Matchers.anyString())
        // but the production code constructs FileInputStream from a File (not a String),
        // so that stub never actually matched and had no effect - the 404 below already
        // came from the real FileInputStream(File) constructor throwing FileNotFoundException
        // for a non-existent path. Mockito's MockedConstruction mocks every constructor
        // invocation regardless of argument types, so introducing one here would swallow
        // that real FileNotFoundException and change the test's outcome. We therefore only
        // mock BahmniCoreProperties, matching the original's true (if accidental) behavior.
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn("");
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory.defaultImage")).thenReturn("");
            patientDocumentService = new PatientDocumentServiceImpl();

            ResponseEntity<Object> responseEntity = patientDocumentService.retriveImage("patientUuid");

            assertEquals(404, responseEntity.getStatusCode().value());
        }
    }

    @Test
    public void shouldNotReadFileOutsideImagesDirectoryViaPathTraversal() throws Exception {
        File imagesDir = temporaryFolder.newFolder("images");
        File outsideDir = temporaryFolder.newFolder("outside");
        File secretFile = new File(outsideDir, "secret.jpeg");
        byte[] secretBytes = "TOP-SECRET-CONTENT-NOT-A-PATIENT-PHOTO".getBytes();
        FileUtils.writeByteArrayToFile(secretFile, secretBytes);

        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn(imagesDir.getAbsolutePath());
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.images.directory.defaultImage")).thenReturn("");

            patientDocumentService = new PatientDocumentServiceImpl();

            String traversalPayload = "../outside/secret";
            ResponseEntity<Object> responseEntity = patientDocumentService.retriveImageWithoutDefault(traversalPayload);

            assertEquals(404, responseEntity.getStatusCode().value());
        }
    }

    @Test
    public void shouldThrowExceptionWhenVideoFormatIsNotSupported() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class);
             MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            expectedException.expect(VideoFormatNotSupportedException.class);
            expectedException.expectMessage(String.format("The video format '%s' is not supported. Supported formats are %s",
                    "xyz", Arrays.toString(VideoFormats.values())));

            patientDocumentService = new PatientDocumentServiceImpl();
            patientDocumentService.saveDocument(1, "Consultation", "videoContent", "xyz", "video", "file-name");
        }
    }

    @Test
    public void shouldSavePDF() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class);
             MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            patientDocumentService = new PatientDocumentServiceImpl();
            String url = patientDocumentService.saveDocument(1, "Consultation", "pdfContent", "pdf", "file", "file-name");

            assertTrue(url.matches(".*1-Consultation-.*.pdf"));
        }
    }

    @Test
    public void shouldSaveImage() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class);
             MockedStatic<ImageIO> imageIOMockedStatic = Mockito.mockStatic(ImageIO.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
            BufferedImage bufferedImage = new BufferedImage(1, 2, 2);
            imageIOMockedStatic.when(() -> ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
            imageIOMockedStatic.when(() -> ImageIO.write(eq(bufferedImage), eq("jpg"), Matchers.any(File.class))).thenReturn(true);

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            patientDocumentService = new PatientDocumentServiceImpl();
            String url = patientDocumentService.saveDocument(1, "Consultation", "imageContent", "jpg", "image", "file-name");

            assertTrue(url.matches(".*1-Consultation-.*.jpg"));
        }
    }

    @Test
    public void shouldNotWriteDocumentOutsideBaseDirectoryViaPathTraversal() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory"))
                    .thenReturn(temporaryFolder.getRoot().getAbsolutePath());

            patientDocumentService = new PatientDocumentServiceImpl();

            expectedException.expect(BahmniCoreException.class);
            patientDocumentService.saveDocument(1, "Consultation", "pdfContent", "pdf", "file",
                    "../../../../pwned_by_bahmni_poc");
        }
    }

    @Test
    public void shouldThrowExceptionWhenFileTypeIsNotSupported() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class);
             MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            expectedException.expect(FileTypeNotSupportedException.class);
            expectedException.expectMessage("The file type is not supported. Supported types are image/video/pdf");

            patientDocumentService = new PatientDocumentServiceImpl();
            patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "xyz", "csv", "file-name");
        }
    }

    @Test
    public void shouldThrowExceptionWhenImageTypeOtherThanPngJpegGif() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class);
             MockedStatic<FileUtils> fileUtilsMockedStatic = Mockito.mockStatic(FileUtils.class);
             MockedStatic<ImageIO> imageIOMockedStatic = Mockito.mockStatic(ImageIO.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
            BufferedImage bufferedImage = new BufferedImage(1, 2, 2);
            imageIOMockedStatic.when(() -> ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
            imageIOMockedStatic.when(() -> ImageIO.write(eq(bufferedImage), eq("bmp"), Matchers.any(File.class))).thenReturn(false);

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            expectedException.expect(FileTypeNotSupportedException.class);
            expectedException.expectMessage("The image format 'bmp' is not supported. Supported formats are [png, jpeg, gif]");

            patientDocumentService = new PatientDocumentServiceImpl();
            patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "bmp", "image", "file-name");
        }
    }

    @Test
    public void shouldCreateThumbnailForVideo() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            byte[] allBytes = Files.readAllBytes(Paths.get("src/test/resources/SampleVideo.mov"));
            String content = Base64.encode(allBytes);
            List<ThumbnailGenerator> thumbnailGenerators = new ArrayList<>();
            thumbnailGenerators.add(thumbnailGenerator);

            patientDocumentService = new PatientDocumentServiceImpl();
            patientDocumentService.setThumbnailGenerators(thumbnailGenerators);
            when(thumbnailGenerator.isFormatSupported("mov")).thenReturn(true);
            when(thumbnailGenerator.generateThumbnail(any(File.class))).thenReturn(new BufferedImage(20, 20, 1));
            patientDocumentService.saveDocument(1, "Consultation", content, "mov", "video", "file-name");
            verify(thumbnailGenerator, times(1)).isFormatSupported("mov");
            verify(thumbnailGenerator, times(1)).generateThumbnail(any(File.class));
        }
    }

    @Test
    public void shouldNotCreateThumbnailForVideo() throws Exception {
        try (MockedStatic<BahmniCoreProperties> bahmniCorePropertiesMockedStatic = Mockito.mockStatic(BahmniCoreProperties.class)) {
            bahmniCorePropertiesMockedStatic.when(() -> BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

            Patient patient = new Patient();
            patient.setId(1);
            patient.setUuid("patient-uuid");

            byte[] allBytes = Files.readAllBytes(Paths.get("src/test/resources/SampleVideo.mov"));
            String content = Base64.encode(allBytes);
            List<ThumbnailGenerator> thumbnailGenerators = new ArrayList<>();
            thumbnailGenerators.add(thumbnailGenerator);

            patientDocumentService = new PatientDocumentServiceImpl();
            patientDocumentService.setThumbnailGenerators(thumbnailGenerators);
            when(thumbnailGenerator.isFormatSupported("mkv")).thenReturn(false);
            when(thumbnailGenerator.generateThumbnail(any(File.class))).thenReturn(new BufferedImage(20, 20, 1));
            patientDocumentService.saveDocument(1, "Consultation", content, "mkv", "video", "file-name");
            verify(thumbnailGenerator, times(1)).isFormatSupported("mkv");
            verify(thumbnailGenerator, times(0)).generateThumbnail(any(File.class));
        }
    }
}
