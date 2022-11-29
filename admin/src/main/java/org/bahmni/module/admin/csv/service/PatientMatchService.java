package org.bahmni.module.admin.csv.service;

import groovy.lang.GroovyClassLoader;
import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.patientmatchingalgorithm.BahmniPatientMatchingAlgorithm;
import org.bahmni.module.admin.csv.patientmatchingalgorithm.PatientMatchingAlgorithm;
import org.bahmni.module.admin.csv.patientmatchingalgorithm.exception.CannotMatchPatientException;
import org.bahmni.module.bahmnicommons.api.service.BahmniPatientService;
import org.openmrs.Patient;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PatientMatchService {
    @Autowired
    private BahmniPatientService patientService;

    private static final String PATIENT_MATCHING_ALGORITHM_DIRECTORY = "/patientMatchingAlgorithm/";
    private static final Logger log = LogManager.getLogger(PatientMatchService.class);

    // Mujir - an implementation could use multiple patient matching algorithms
    protected Map<String, PatientMatchingAlgorithm> patientMatchingAlgorithms = new HashMap<>();

    public Patient getPatient(String matchingAlgorithmClassName, List<KeyValue> patientAttributes, String patientIdentifier, boolean shouldMatchExactPatientId) throws IOException, IllegalAccessException, InstantiationException, CannotMatchPatientException {
        List<Patient> matchingPatients = patientService.get(patientIdentifier, shouldMatchExactPatientId);
        return matchPatients(matchingPatients, patientAttributes, matchingAlgorithmClassName);
    }

    private Patient matchPatients(List<Patient> matchingPatients, List<KeyValue> patientAttributes, String matchingAlgorithmClassName) throws IOException, IllegalAccessException, InstantiationException, CannotMatchPatientException {
        if (StringUtils.isEmpty(matchingAlgorithmClassName)) {
            return new BahmniPatientMatchingAlgorithm().run(matchingPatients, patientAttributes);
        }
        PatientMatchingAlgorithm patientMatchingAlgorithm = getPatientMatchingAlgorithm(matchingAlgorithmClassName);
        log.debug("PatientMatching : Using Algorithm in " + patientMatchingAlgorithm.getClass().getName());
        return patientMatchingAlgorithm.run(matchingPatients, patientAttributes);
    }

    private PatientMatchingAlgorithm getPatientMatchingAlgorithm(String matchingAlgorithmClassName) throws IOException, InstantiationException, IllegalAccessException {
        PatientMatchingAlgorithm patientMatchingAlgorithm = patientMatchingAlgorithms.get(matchingAlgorithmClassName);
        if (patientMatchingAlgorithm == null) {
            Class clazz = new GroovyClassLoader().parseClass(new File(getAlgorithmClassPath(matchingAlgorithmClassName)));
            patientMatchingAlgorithms.put(matchingAlgorithmClassName, (PatientMatchingAlgorithm) clazz.newInstance());
        }

        return patientMatchingAlgorithms.get(matchingAlgorithmClassName);
    }

    private String getAlgorithmClassPath(String matchingAlgorithmClassName) {
        return OpenmrsUtil.getApplicationDataDirectory() + PATIENT_MATCHING_ALGORITHM_DIRECTORY + matchingAlgorithmClassName;
    }

}
