DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.highRiskPatients'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.highRiskPatients',
        'SELECT DISTINCT
  concat(pn.given_name, " ", pn.family_name)           AS name,
  pi.identifier                                        AS identifier,
  concat("", p.uuid)                                   AS uuid,
  concat("", v.uuid)                                   AS activeVisitUuid,
  IF(va.value_reference = "Admitted", "true", "false") AS hasBeenAdmitted
FROM person p
  INNER JOIN person_name pn ON pn.person_id = p.person_id
  INNER JOIN patient_identifier pi ON pn.person_id = pi.patient_id
  INNER JOIN visit v ON v.patient_id = p.person_id AND v.date_stopped IS NULL AND v.voided = 0
  INNER JOIN (SELECT
                max(test_obs.obs_group_id) AS max_id,
                test_obs.concept_id,
                test_obs.person_id
              FROM obs test_obs
                INNER JOIN concept c ON c.concept_id = test_obs.concept_id AND test_obs.voided = 0
                INNER JOIN concept_name cn
                  ON c.concept_id = cn.concept_id AND cn.concept_name_type = "FULLY_SPECIFIED" AND
                     cn.name IN (${testName})
              GROUP BY test_obs.person_id, test_obs.concept_id) AS tests ON tests.person_id = v.patient_id
  INNER JOIN obs abnormal_obs
    ON abnormal_obs.obs_group_id = tests.max_id AND abnormal_obs.value_coded = 1 AND abnormal_obs.voided = 0
  INNER JOIN concept abnormal_concept ON abnormal_concept.concept_id = abnormal_obs.concept_id
  INNER JOIN concept_name abnormal_concept_name
    ON abnormal_concept.concept_id = abnormal_concept_name.concept_id AND
       abnormal_concept_name.concept_name_type = "FULLY_SPECIFIED" AND
       abnormal_concept_name.name IN ("LAB_ABNORMAL")
  LEFT OUTER JOIN visit_attribute va ON va.visit_id = v.visit_id AND va.attribute_type_id =
                                                                     (SELECT visit_attribute_type_id
                                                                      FROM visit_attribute_type
                                                                      WHERE name = "Admission Status")',
        'SQL QUERY TO get LIST of patients who has pending orders',
        uuid()
);
