package org.bahmni.module.bahmnicore.service.impl;

import org.apache.commons.io.FileUtils;
import org.apache.xerces.impl.dv.util.Base64;
import org.bahmni.module.bahmnicore.BaseIntegrationTest;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.junit.Test;
import org.openmrs.Patient;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertTrue;

public class PatientDocumentServiceImplIT extends BaseIntegrationTest{

	public static final String TMP_FOLDER = "/tmp";
	@Autowired
	PatientDocumentServiceImpl patientDocumentService;


	@Test
	public void shouldNotCreateThumbnailForVideo() throws Exception {

		Patient patient = new Patient();
		patient.setId(1);
		patient.setUuid("patient-uuid");

		FileUtils.writeStringToFile(new File(TMP_FOLDER + "/bahmnicore.properties"),
				"bahmnicore.documents.baseDirectory=" + TMP_FOLDER);
		OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);

		BahmniCoreProperties.load();

		byte[] allBytes = Files.readAllBytes(Paths.get("src/test/resources/SampleVideo.mkv"));
		String content = Base64.encode(allBytes);
		String url = patientDocumentService.saveDocument(1, "Consultation", content, "mkv", "video", "file-name");
		assertTrue(url.matches(".*1-Consultation-.*.mkv"));
		String videoUrl = BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")  + "/" + url;
		String thumbnailUrl  = BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")  + "/" + url.split("\\.")[0] + "_thumbnail.jpg";
		assertTrue(Files.exists(Paths.get(videoUrl)));
		assertFalse(Files.exists(Paths.get(thumbnailUrl)));
	}

	@Test
	public void shouldCreateThumbnailForVideo() throws Exception {

		Patient patient = new Patient();
		patient.setId(1);
		patient.setUuid("patient-uuid");

		FileUtils.writeStringToFile(new File(TMP_FOLDER + "/bahmnicore.properties"),
				"bahmnicore.documents.baseDirectory=" + TMP_FOLDER);
		OpenmrsUtil.setApplicationDataDirectory(TMP_FOLDER);

		BahmniCoreProperties.load();

		byte[] allBytes = Files.readAllBytes(Paths.get("src/test/resources/SampleVideo.mov"));
		String content = Base64.encode(allBytes);
		String url = patientDocumentService.saveDocument(1, "Consultation", content, "mov", "video", "file-name");
		assertTrue(url.matches(".*1-Consultation-.*.mov"));
		String videoUrl = BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")  + "/" + url;
		String thumbnailUrl  = BahmniCoreProperties.getProperty("bahmnicore.documents.baseDirectory")  + "/" + url.split("\\.")[0] + "_thumbnail.jpg";
		assertTrue(Files.exists(Paths.get(videoUrl)));
		assertTrue(Files.exists(Paths.get(thumbnailUrl)));
	}
}
