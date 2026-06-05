package org.bahmni.jss;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.bahmni.csv.MigrateResult;
import org.bahmni.csv.MigratorBuilder;
import org.bahmni.csv.exception.MigrationException;
import org.bahmni.datamigration.AddressService;
import org.bahmni.datamigration.AllLookupValues;
import org.bahmni.datamigration.AmbiguousTehsils;
import org.bahmni.datamigration.CorrectedTehsils;
import org.bahmni.datamigration.MasterTehsils;
import org.bahmni.datamigration.csv.Patient;
import org.bahmni.datamigration.csv.PatientPersister;
import org.bahmni.openmrsconnector.AllPatientAttributeTypes;
import org.bahmni.openmrsconnector.OpenMRSRESTConnection;
import org.bahmni.openmrsconnector.OpenMRSRestService;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.sql.SQLException;
import java.util.HashMap;

public class JSSMigrator {
    private final OpenMRSRestService openMRSRestService;
    private final HashMap<String, AllLookupValues> lookupValuesMap;
    private String csvLocation;
    private final int numberOfValidationThreads;
    private final int numberOfMigrationThreads;
    private static Logger logger = LogManager.getLogger(JSSMigrator.class);

    public static void main(String[] args) throws URISyntaxException, IOException, ClassNotFoundException, SQLException, InterruptedException {
        if (args.length < 2) {
            logger.error(String.format("Usage %s CSV-File-Location RegistrationCSVFileName", JSSMigrator.class.getName()));
            logPropertyUsage("localhost", "root", "password", "admin", "test");
            System.exit(1);
        }
        String csvLocation = args[0];
        String registrationCSVFileName = args[1];
        int numberOfValidationThreads = 1;
        int numberOfMigrationThreads = 20;
        if(args[2] != null)
            numberOfValidationThreads = Integer.valueOf(args[2]);
        if(args[3] != null)
            numberOfMigrationThreads = Integer.valueOf(args[3]);

        logger.info(String.format("Using CSVFileLocation=%s; RegistrationFileName=%s", new File(csvLocation).getAbsolutePath(), registrationCSVFileName));
        String openMRSHostName = System.getProperty("openmrs.host.name", "localhost");
        String databaseUserId = System.getProperty("database.user.id", "root");
        String databasePassword = System.getProperty("database.user.password", "password");
        String openmrsUserId = System.getProperty("openmrs.user.id", "admin");
        String openmrsUserPassword = System.getProperty("openmrs.user.password", "Admin123");
        logPropertyUsage(openMRSHostName, databaseUserId, databasePassword, openmrsUserId, openmrsUserPassword);

        OpenMRSRESTConnection openMRSRESTConnection = new OpenMRSRESTConnection(openMRSHostName, openmrsUserId, openmrsUserPassword);
        MasterTehsils masterTehsils = new MasterTehsils(csvLocation, "MasterTehsils.csv");
        AmbiguousTehsils ambiguousTehsils = new AmbiguousTehsils(csvLocation, "AmbiguousTehsils.txt");
        CorrectedTehsils correctedTehsils = new CorrectedTehsils(csvLocation, "CorrectedTehsils.csv");
        AddressService addressService = new AddressService(masterTehsils, ambiguousTehsils, correctedTehsils);

        JSSMigrator jssMigrator = new JSSMigrator(csvLocation, "LU_Caste.csv", "LU_District.csv", "LU_State.csv",
                "LU_Class.csv", "LU_Tahsil.csv", openMRSRESTConnection, numberOfValidationThreads, numberOfMigrationThreads);
        jssMigrator.migratePatient(registrationCSVFileName, addressService, openMRSRESTConnection);
    }

    private static void logPropertyUsage(String openMRSHostName, String databaseUserId, String databaseUserPassword, String openmrsUserId, String openmrsPassword) {
        logger.printf(Level.INFO, "By default uses following properties: openmrs.host.name=%s; database.user.id=%s; openmrs.user.id=%s;", openMRSHostName, databaseUserId, openmrsUserId);
   }

    public JSSMigrator(String csvLocation, String casteFileName, String districtFileName, String stateFileName,
                       String classFileName, String tahsilFileName, OpenMRSRESTConnection openMRSRESTConnection,
                       int numberOfValidationThreads, int numberOfMigrationThreads) throws IOException,
            URISyntaxException {
        this.csvLocation = csvLocation;
        this.numberOfValidationThreads = numberOfValidationThreads;
        this.numberOfMigrationThreads = numberOfMigrationThreads;
        AllLookupValues allCastes = new AllLookupValues(csvLocation, casteFileName);
        AllLookupValues allDistricts = new AllLookupValues(csvLocation, districtFileName);
        AllLookupValues allStates = new AllLookupValues(csvLocation, stateFileName);
        AllLookupValues allClasses = new AllLookupValues(csvLocation, classFileName);
        AllLookupValues allTahsils = new AllLookupValues(csvLocation, tahsilFileName);
        lookupValuesMap = new HashMap<>();
        lookupValuesMap.put("Castes", allCastes);
        lookupValuesMap.put("Districts", allDistricts);
        lookupValuesMap.put("States", allStates);
        lookupValuesMap.put("Classes", allClasses);
        lookupValuesMap.put("Tahsils", allTahsils);

        openMRSRestService = new OpenMRSRestService(openMRSRESTConnection);
    }

    public void migratePatient(String csvFileName, AddressService addressService, OpenMRSRESTConnection openMRSRESTConnection) throws IOException {
        AllPatientAttributeTypes allPatientAttributeTypes = openMRSRestService.getAllPatientAttributeTypes();

        PatientPersister patientPersister = new PatientPersister(lookupValuesMap, addressService,
                                                        allPatientAttributeTypes, openMRSRESTConnection, openMRSRestService.getSessionId());
        org.bahmni.csv.Migrator migrator = new MigratorBuilder(Patient.class)
                                                        .readFrom(csvLocation, csvFileName)
                                                        .persistWith(patientPersister)
                                                        .withMultipleValidators(numberOfValidationThreads)
                                                        .withMultipleMigrators(numberOfMigrationThreads)
                                                        .build();
        try {
            MigrateResult migrateResult = migrator.migrate();
            logger.info("Migration was {}", (migrateResult.hasFailed() ? "unsuccessful" : "successful"));
            logger.info("Stage : {} . Success count : {} . Fail count : {}", migrateResult.getStageName(), migrateResult.numberOfSuccessfulRecords(), migrateResult.numberOfFailedRecords());
        } catch (MigrationException e) {
            logger.error("There was an error during migration. {}", e.getMessage());
        }
    }
}