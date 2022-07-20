package org.bahmni.module.bahmnicore.web.v1_0.controller;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.common.config.registration.service.RegistrationPageReaderService;
import org.bahmni.common.config.registration.service.RegistrationPageService;
import org.bahmni.common.config.registration.service.impl.RegistrationPageReaderServiceImpl;
import org.bahmni.common.config.registration.service.impl.RegistrationPageServiceImpl;
import org.bahmni.common.db.JDBCConnectionProvider;
import org.bahmni.csv.CSVFile;
import org.bahmni.csv.EntityPersister;
import org.bahmni.fileimport.FileImporter;
import org.bahmni.fileimport.ImportStatus;
import org.bahmni.fileimport.dao.ImportStatusDao;
import org.bahmni.module.admin.csv.models.ConceptRow;
import org.bahmni.module.admin.csv.models.ConceptSetRow;
import org.bahmni.module.admin.csv.models.DrugRow;
import org.bahmni.module.admin.csv.models.LabResultsRow;
import org.bahmni.module.admin.csv.models.MultipleEncounterRow;
import org.bahmni.module.admin.csv.models.PatientProgramRow;
import org.bahmni.module.admin.csv.models.PatientRow;
import org.bahmni.module.admin.csv.models.FormerConceptReferenceRow;
import org.bahmni.module.admin.csv.models.ReferenceTermRow;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.bahmni.module.admin.csv.persister.ConceptPersister;
import org.bahmni.module.admin.csv.persister.ConceptReferenceTermPersister;
import org.bahmni.module.admin.csv.persister.ConceptSetPersister;
import org.bahmni.module.admin.csv.persister.DatabasePersister;
import org.bahmni.module.admin.csv.persister.DrugPersister;
import org.bahmni.module.admin.csv.persister.EncounterPersister;
import org.bahmni.module.admin.csv.persister.LabResultPersister;
import org.bahmni.module.admin.csv.persister.PatientPersister;
import org.bahmni.module.admin.csv.persister.PatientProgramPersister;
import org.bahmni.module.admin.csv.persister.ReferenceTermPersister;
import org.bahmni.module.admin.csv.persister.RelationshipPersister;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.engine.spi.SessionImplementor;
import org.hibernate.internal.SessionImpl;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.v1_0.controller.BaseRestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class AdminImportController extends BaseRestController {
    private final String baseUrl = "/rest/" + RestConstants.VERSION_1 + "/bahmnicore/admin/upload";
    private static Logger logger = LogManager.getLogger(AdminImportController.class);

    public static final String YYYY_MM_DD_HH_MM_SS = "_yyyy-MM-dd_HH:mm:ss";
    private static final int DEFAULT_NUMBER_OF_DAYS = 30;
    private static final String HTTPS_PROTOCOL = "https";
    private static final String HTTP_PROTOCOL = "http";

    public static final String PARENT_DIRECTORY_UPLOADED_FILES_CONFIG = "uploaded.files.directory";
    public static final String SHOULD_MATCH_EXACT_PATIENT_ID_CONFIG = "uploaded.should.matchExactPatientId";

    private static final boolean DEFAULT_SHOULD_MATCH_EXACT_PATIENT_ID = false;
    public static final String ENCOUNTER_FILES_DIRECTORY = "encounter/";
    private static final String PROGRAM_FILES_DIRECTORY = "program/";
    private static final String CONCEPT_FILES_DIRECTORY = "concept/";
    private static final String LAB_RESULTS_DIRECTORY = "labResults/";
    private static final String DRUG_FILES_DIRECTORY = "drug/";
    private static final String CONCEPT_SET_FILES_DIRECTORY = "conceptset/";
    private static final String PATIENT_FILES_DIRECTORY = "patient/";
    private static final String REFERENCETERM_FILES_DIRECTORY = "referenceterms/";
    private static final String RELATIONSHIP_FILES_DIRECTORY = "relationship/";

    @Autowired
    private EncounterPersister encounterPersister;

    @Autowired
    private PatientProgramPersister patientProgramPersister;

    @Autowired
    private DrugPersister drugPersister;

    @Autowired
    private ConceptPersister conceptPersister;

    @Autowired
    private LabResultPersister labResultPersister;

    @Autowired
    private ConceptSetPersister conceptSetPersister;

    @Autowired
    private PatientPersister patientPersister;

    @Autowired
    private ReferenceTermPersister referenceTermPersister;

    @Autowired
    private RelationshipPersister relationshipPersister;

    @Autowired
    private ConceptReferenceTermPersister conceptReferenceTermPersister;

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    @Qualifier("adminService")
    private AdministrationService administrationService;

    private RegistrationPageReaderService registrationPageReaderService = new RegistrationPageReaderServiceImpl();

    private RegistrationPageService registrationPageService = new RegistrationPageServiceImpl(registrationPageReaderService);

    @RequestMapping(value = baseUrl + "/patient", method = RequestMethod.POST)
    @ResponseBody
        public boolean upload(@RequestParam(value = "file") MultipartFile file, @RequestHeader("Host") String host, @RequestHeader(value = "Origin", required = false) String origin, @RequestHeader(value = "Referer", required = false) String referer) throws IOException {
        try {
            registrationPageService.setProtocol(getProtocol(origin, referer));
            registrationPageService.setHost(host);
            patientPersister.init(Context.getUserContext());
            return importCsv(PATIENT_FILES_DIRECTORY, file, patientPersister, 1, true, PatientRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/encounter", method = RequestMethod.POST)
    @ResponseBody
    public boolean upload(@CookieValue(value="bahmni.user.location", required=true) String loginCookie,
                          @RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) throws IOException {

        return uploadEncounter(loginCookie, file, patientMatchingAlgorithm, false);
    }

    @RequestMapping(value = baseUrl + "/form2encounter", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadForm2EncountersWithValidations(@CookieValue(value="bahmni.user.location", required=true) String loginCookie,
                          @RequestParam(value = "file") MultipartFile file,
                          @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) throws IOException {

        return uploadEncounter(loginCookie, file, patientMatchingAlgorithm, true);
    }

    private boolean uploadEncounter(@CookieValue(value = "bahmni.user.location", required = true) String loginCookie, @RequestParam("file") MultipartFile file, @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm, boolean performForm2Validations) throws IOException {
        try {
            String configuredExactPatientIdMatch = administrationService.getGlobalProperty(SHOULD_MATCH_EXACT_PATIENT_ID_CONFIG);
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(loginCookie);
            String loginUuid =  jsonObject.get("uuid").getAsString();
            boolean shouldMatchExactPatientId = DEFAULT_SHOULD_MATCH_EXACT_PATIENT_ID;
            if (configuredExactPatientIdMatch != null)
                shouldMatchExactPatientId = Boolean.parseBoolean(configuredExactPatientIdMatch);

            encounterPersister.init(Context.getUserContext(), patientMatchingAlgorithm, shouldMatchExactPatientId, loginUuid, performForm2Validations);
            return importCsv(ENCOUNTER_FILES_DIRECTORY, file, encounterPersister, 5, true, MultipleEncounterRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/referenceterms", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadReferenceTerms(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            referenceTermPersister.init(Context.getUserContext());
            return importCsv(REFERENCETERM_FILES_DIRECTORY, file, referenceTermPersister, 1, true, ReferenceTermRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }

    }

    @RequestMapping(value = baseUrl + "/referenceterms/new", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadReferenceTermsForExistingConcepts(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            conceptReferenceTermPersister.init(Context.getUserContext());
            return importCsv(REFERENCETERM_FILES_DIRECTORY, file, new DatabasePersister<>(conceptReferenceTermPersister), 1, false, FormerConceptReferenceRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }

    }

    @RequestMapping(value = baseUrl + "/program", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadProgram(@RequestParam(value = "file") MultipartFile file, @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) throws IOException {
        try {
            patientProgramPersister.init(Context.getUserContext(), patientMatchingAlgorithm);
            return importCsv(PROGRAM_FILES_DIRECTORY, file, patientProgramPersister, 1, true, PatientProgramRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/drug", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadDrug(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            return importCsv(DRUG_FILES_DIRECTORY, file, new DatabasePersister<>(drugPersister), 1, false, DrugRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/concept", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadConcept(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            return importCsv(CONCEPT_FILES_DIRECTORY, file, new DatabasePersister<>(conceptPersister), 1, false, ConceptRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/labResults", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadLabResults(@CookieValue(value="bahmni.user.location", required=true) String loginCookie, @RequestParam(value = "file") MultipartFile file, @RequestParam(value = "patientMatchingAlgorithm", required = false) String patientMatchingAlgorithm) throws IOException {
        try {
            JsonParser jsonParser = new JsonParser();
            JsonObject jsonObject = (JsonObject) jsonParser.parse(loginCookie);
            String loginUuid =  jsonObject.get("uuid").getAsString();
            labResultPersister.init(Context.getUserContext(), patientMatchingAlgorithm, true,loginUuid);
            return importCsv(LAB_RESULTS_DIRECTORY, file, new DatabasePersister<>(labResultPersister), 1, false, LabResultsRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/conceptset", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadConceptSet(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            return importCsv(CONCEPT_SET_FILES_DIRECTORY, file, new DatabasePersister<>(conceptSetPersister), 1, false, ConceptSetRow.class);
        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/relationship", method = RequestMethod.POST)
    @ResponseBody
    public boolean uploadRelationship(@RequestParam(value = "file") MultipartFile file) throws IOException {
        try {
            relationshipPersister.init(Context.getUserContext());
            return importCsv(RELATIONSHIP_FILES_DIRECTORY, file, new DatabasePersister<>(relationshipPersister), 1, false, RelationshipRow.class);

        } catch (Throwable e) {
            logger.error("Could not upload file", e);
            throw e;
        }
    }

    @RequestMapping(value = baseUrl + "/status", method = RequestMethod.GET)
    @ResponseBody
    public List<ImportStatus> status(@RequestParam(required = false) Integer numberOfDays) throws SQLException {
        numberOfDays = numberOfDays == null ? DEFAULT_NUMBER_OF_DAYS : numberOfDays;
        ImportStatusDao importStatusDao = new ImportStatusDao(new CurrentThreadConnectionProvider());
        return importStatusDao.getImportStatusFromDate(DateUtils.addDays(new Date(), (numberOfDays * -1)));
    }

    private <T extends org.bahmni.csv.CSVEntity> boolean importCsv(String filesDirectory, MultipartFile file, EntityPersister<T> persister,
                                                                   int numberOfThreads, boolean skipValidation, Class entityClass) throws IOException {
        String uploadedOriginalFileName = ((CommonsMultipartFile) file).getFileItem().getName();
        String systemId = Context.getUserContext().getAuthenticatedUser().getSystemId();
        CSVFile persistedUploadedFile = writeToLocalFile(file, filesDirectory);
        return new FileImporter<T>().importCSV(uploadedOriginalFileName, persistedUploadedFile,
                persister, entityClass, new NewMRSConnectionProvider(), systemId, skipValidation, numberOfThreads);
    }


    private CSVFile writeToLocalFile(MultipartFile file, String filesDirectory) throws IOException {
        String uploadedOriginalFileName = ((CommonsMultipartFile) file).getFileItem().getName();
        byte[] fileBytes = file.getBytes();
        CSVFile uploadedFile = getFile(uploadedOriginalFileName, filesDirectory);
        FileOutputStream uploadedFileStream = null;
        try {
            uploadedFileStream = new FileOutputStream(new File(uploadedFile.getAbsolutePath()));
            uploadedFileStream.write(fileBytes);
            uploadedFileStream.flush();
        } catch (Throwable e) {
            logger.error(e);
            throw e;
            // TODO : handle errors for end users. Give some good message back to users.
        } finally {
            if (uploadedFileStream != null) {
                try {
                    uploadedFileStream.close();
                } catch (IOException e) {
                    logger.error(e);
                }
            }
            return uploadedFile;
        }
    }

    private CSVFile getFile(String fileName, String filesDirectory) throws IOException {
        String fileNameWithoutExtension = fileName.substring(0, fileName.lastIndexOf("."));
        String fileExtension = fileName.substring(fileName.lastIndexOf("."));

        String timestampForFile = new SimpleDateFormat(YYYY_MM_DD_HH_MM_SS).format(new Date());

        String uploadDirectory = administrationService.getGlobalProperty(PARENT_DIRECTORY_UPLOADED_FILES_CONFIG);
        String relativePath = filesDirectory + fileNameWithoutExtension + timestampForFile + fileExtension;
        FileUtils.forceMkdir(new File(uploadDirectory, filesDirectory));
        return new CSVFile(uploadDirectory, relativePath);
    }

    private class NewMRSConnectionProvider implements JDBCConnectionProvider {
        private ThreadLocal<Session> session = new ThreadLocal<>();

        @Override
        public Connection getConnection() {
            if (session.get() == null || !session.get().isOpen())
                session.set(sessionFactory.openSession());

            return ((SessionImpl)session.get()).connection();
        }

        @Override
        public void closeConnection() {
            session.get().close();
        }
    }

    private class CurrentThreadConnectionProvider implements JDBCConnectionProvider {
        @Override
        public Connection getConnection() {
            //TODO: ensure that only connection associated with current thread current transaction is given
            SessionImplementor session = (SessionImpl) sessionFactory.getCurrentSession();
            return session.connection();
        }

        @Override
        public void closeConnection() {

        }
    }

    private String getProtocol(String origin, String referer) {
        if(origin != null) {
            if(origin.startsWith(HTTPS_PROTOCOL))
                return HTTPS_PROTOCOL;
            else
                return HTTP_PROTOCOL;
        }

        if(referer != null) {
            if(referer.startsWith(HTTPS_PROTOCOL))
                return HTTPS_PROTOCOL;
            else
                return HTTP_PROTOCOL;
        }

        return HTTPS_PROTOCOL;
    }
}
