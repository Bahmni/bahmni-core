DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.highRiskPatients'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.highRiskPatients',
        'SELECT DISTINCT
        concat(pn.given_name, '' '', pn.family_name) AS name,
        pi.identifier                                        AS identifier,
        concat("", p.uuid)                                   AS uuid,
        concat("", v.uuid)                                   AS activeVisitUuid,
        IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        FROM person p
        INNER JOIN person_name pn ON pn.person_id = p.person_id
        INNER JOIN patient_identifier pi ON pn.person_id = pi.patient_id
        INNER JOIN visit v ON v.patient_id = p.person_id AND v.date_stopped IS NULL AND v.voided = 0
        INNER JOIN (SELECT max(obs_group_id) AS max_id, concept_id, person_id
        FROM obs
        WHERE voided = 0 AND concept_id IN
        (SELECT concept_id
        FROM concept_name cn
        WHERE cn.concept_name_type = \'FULLY_SPECIFIED\' AND
        cn.name IN (${testName}))
        GROUP BY person_id, concept_id)  AS tests ON tests.person_id = v.patient_id
        INNER JOIN (SELECT obs_group_id, value_coded
        FROM obs
        WHERE voided = 0 AND concept_id IN
        (SELECT concept_id
        FROM concept_name cn
        WHERE cn.concept_name_type = \'FULLY_SPECIFIED\' AND
        cn.name = \'LAB_ABNORMAL\')) AS abnormal ON abnormal.obs_group_id = tests.max_id AND abnormal.value_coded =1

        LEFT OUTER JOIN visit_attribute va ON va.visit_id = v.visit_id AND va.attribute_type_id =
        (SELECT visit_attribute_type_id
        FROM visit_attribute_type
        WHERE NAME = "Admission Status")',
        'SQL QUERY TO get LIST of patients who has pending orders',
        uuid()
        );
