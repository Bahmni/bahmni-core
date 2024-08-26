package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.bahmni.module.bahmnicore.bahmniexceptions.FileTypeNotSupportedException;
import org.bahmni.module.bahmnicore.bahmniexceptions.VideoFormatNotSupportedException;
import org.bahmni.module.bahmnicore.model.VideoFormats;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.bahmni.module.bahmnicore.service.ThumbnailGenerator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.openmrs.Patient;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.http.ResponseEntity;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest({BahmniCoreProperties.class, FileInputStream.class, FileUtils.class, ImageIO.class, Context.class})
public class PatientDocumentServiceImplTest {

    private PatientDocumentServiceImpl patientDocumentService;
    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Mock
    ThumbnailGenerator thumbnailGenerator;

    @Mock
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
    }

    @Test
    public void shouldCreateRightDirectoryAccordingToPatientId() {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");

        patientDocumentService = new PatientDocumentServiceImpl();

        String url = patientDocumentService.createFilePath(".", 280, "Radiology", "jpeg", "file-name");

        assertFalse(url.isEmpty());
        assertTrue(url.startsWith("300/280-Radiology-"));
        assertTrue(url.endsWith(".jpeg"));

        File absoluteFileDirectory = new File("./300");
        absoluteFileDirectory.delete();
    }

    @Test
    public void shouldGetImageNotFoundForIfNoImageCapturedForPatientAndNoDefaultImageNotPresent() throws Exception {
        final FileInputStream fileInputStreamMock = PowerMockito.mock(FileInputStream.class);
        PowerMockito.whenNew(FileInputStream.class).withArguments(Matchers.anyString()).thenReturn(fileInputStreamMock);
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.images.directory")).thenReturn("");
        when(BahmniCoreProperties.getProperty("bahmnicore.images.directory.defaultImage")).thenReturn("");
        patientDocumentService = new PatientDocumentServiceImpl();

        ResponseEntity<Object> responseEntity = patientDocumentService.retriveImage("patientUuid");

        assertEquals(404, responseEntity.getStatusCode().value());
    }

    @Test
    public void shouldThrowExceptionWhenVideoFormatIsNotSupported() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(VideoFormatNotSupportedException.class);
        expectedException.expectMessage(String.format("The video format '%s' is not supported. Supported formats are %s",
                "xyz", Arrays.toString(VideoFormats.values())));

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "videoContent", "xyz", "video", "file-name");
    }

    @Test
    public void shouldSavePDF() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", "pdfContent", "pdf", "file", "file-name");

        assertTrue(url.matches(".*1-Consultation-.*.pdf"));
    }

    @Test
    public void shouldSaveImage() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        PowerMockito.mockStatic(ImageIO.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        BufferedImage bufferedImage = new BufferedImage(1,2, 2);
        when(ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
        when(ImageIO.write(eq(bufferedImage),eq("jpg"), Matchers.any(File.class))).thenReturn(true);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        patientDocumentService = new PatientDocumentServiceImpl();
        String url = patientDocumentService.saveDocument(1, "Consultation", "imageContent", "jpg", "image", "file-name");

        assertTrue(url.matches(".*1-Consultation-.*.jpg"));
    }

    @Test
    public void shouldThrowExceptionWhenFileTypeIsNotSupported() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(FileTypeNotSupportedException.class);
        expectedException.expectMessage("The file type is not supported. Supported types are image/video/pdf");

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "xyz", "csv", "file-name");
    }

    @Test
    public void shouldThrowExceptionWhenImageTypeOtherThanPngJpegGif() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn("");
        PowerMockito.mockStatic(FileUtils.class);
        PowerMockito.mockStatic(ImageIO.class);
        BufferedImage bufferedImage = new BufferedImage(1,2, 2);
        when(ImageIO.read(Matchers.any(ByteArrayInputStream.class))).thenReturn(bufferedImage);
        when(ImageIO.write(eq(bufferedImage),eq("bmp"), Matchers.any(File.class))).thenReturn(false);

        Patient patient = new Patient();
        patient.setId(1);
        patient.setUuid("patient-uuid");

        expectedException.expect(FileTypeNotSupportedException.class);
        expectedException.expectMessage("The image format 'bmp' is not supported. Supported formats are [png, jpeg, gif]");

        patientDocumentService = new PatientDocumentServiceImpl();
        patientDocumentService.saveDocument(1, "Consultation", "otherfileContent", "bmp", "image", "file-name");
    }

    @Test
    public void shouldCreateThumbnailForVideo() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

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
        when(thumbnailGenerator.generateThumbnail(any(File.class))).thenReturn(new BufferedImage(20,20, 1));
        patientDocumentService.saveDocument(1, "Consultation", content, "mov", "video", "file-name");
        verify(thumbnailGenerator,times(1)).isFormatSupported("mov");
        verify(thumbnailGenerator,times(1)).generateThumbnail(any(File.class));
    }

    @Test
    public void shouldNotCreateThumbnailForVideo() throws Exception {
        PowerMockito.mockStatic(BahmniCoreProperties.class);
        when(BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")).thenReturn(temporaryFolder.getRoot().getAbsolutePath());

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
        when(thumbnailGenerator.generateThumbnail(any(File.class))).thenReturn(new BufferedImage(20,20, 1));
        patientDocumentService.saveDocument(1, "Consultation", content, "mkv", "video", "file-name");
        verify(thumbnailGenerator,times(1)).isFormatSupported("mkv");
        verify(thumbnailGenerator,times(0)).generateThumbnail(any(File.class));
    }
}
