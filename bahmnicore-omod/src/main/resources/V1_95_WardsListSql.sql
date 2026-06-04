DELETE FROM global_property where property = 'emrapi.sqlGet.wardsListDetails';

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlGet.wardsListDetails',
"SELECT
  b.bed_number AS 'Bed',
  concat(pn.given_name, ' ', pn.family_name) AS 'Name',
  pv.uuid AS 'Patient Uuid',
  pi.identifier AS 'Id',
  pv.gender AS 'Gender',
  TIMESTAMPDIFF(YEAR, pv.birthdate, CURDATE()) AS 'Age',
  pa.county_district AS 'District',
  pa.city_village AS 'Village',
  admission_provider_name.given_name AS 'Admission By',
  cast(DATE_FORMAT(latestAdmissionEncounter.max_encounter_datetime, '%d %b %y %h:%i %p') AS CHAR) AS 'Admission Time',
  diagnosis.diagnosisConcept AS 'Diagnosis',
  diagnosis.certainty AS 'Diagnosis Certainty',
  diagnosis.diagnosisOrder AS 'Diagnosis Order',
  diagnosis.status AS 'Diagnosis Status',
  diagnosis.diagnosis_provider AS 'Diagnosis Provider',
  cast(DATE_FORMAT(diagnosis.diagnosis_datetime, '%d %b %y %h:%i %p') AS
       CHAR) AS 'Diagnosis Datetime',
  dispositionInfo.providerName AS 'Disposition By',
  cast(DATE_FORMAT(dispositionInfo.providerDate, '%d %b %y %h:%i %p') AS CHAR) AS 'Disposition Time',
  adtNotes.value_text AS 'ADT Notes',
  v.uuid AS 'Visit Uuid'
FROM bed_location_map blm
  INNER JOIN bed b
    ON blm.bed_id = b.bed_id AND
       b.status = 'OCCUPIED' AND
       blm.location_id IN (SELECT child_location.location_id
                           FROM location child_location JOIN
                             location parent_location
                               ON parent_location.location_id =
                                  child_location.parent_location
                           WHERE
                             parent_location.name = ${location_name})
  INNER JOIN bed_patient_assignment_map bpam ON b.bed_id = bpam.bed_id AND date_stopped IS NULL
  INNER JOIN person pv ON pv.person_id = bpam.patient_id
  INNER JOIN person_name pn ON pn.person_id = pv.person_id
  INNER JOIN patient_identifier pi ON pv.person_id = pi.patient_id
  INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
  INNER JOIN global_property gp on gp.property='emr.primaryIdentifierType' and gp.property_value=pit.uuid
  LEFT JOIN person_address pa ON pa.person_id = pv.person_id
  INNER JOIN (SELECT
                patient_id,
                max(encounter_datetime) AS max_encounter_datetime,
                max(visit_id) as visit_id,
                max(encounter_id) AS encounter_id
              FROM encounter
                INNER JOIN encounter_type ON encounter_type.encounter_type_id = encounter.encounter_type
              WHERE encounter_type.name = 'ADMISSION'
              GROUP BY patient_id) latestAdmissionEncounter ON pv.person_id = latestAdmissionEncounter.patient_id
  INNER JOIN visit v ON latestAdmissionEncounter.visit_id = v.visit_id
  LEFT OUTER JOIN obs adtNotes
    ON adtNotes.encounter_id = latestAdmissionEncounter.encounter_id AND adtNotes.voided = 0 AND
       adtNotes.concept_id = (SELECT concept_id
                              FROM concept_name
                              WHERE name = 'Adt Notes' AND concept_name_type = 'FULLY_SPECIFIED')
  LEFT OUTER JOIN encounter_provider ep ON ep.encounter_id = latestAdmissionEncounter.encounter_id
  LEFT OUTER JOIN provider admission_provider ON admission_provider.provider_id = ep.provider_id
  LEFT OUTER JOIN person_name admission_provider_name
    ON admission_provider_name.person_id = admission_provider.person_id
  LEFT OUTER JOIN (
                    SELECT
                      bpam.patient_id AS person_id,
                      concept_name.name AS disposition,
                      latestDisposition.obs_datetime AS providerDate,
                      person_name.given_name AS providerName
                    FROM bed_patient_assignment_map bpam
                      INNER JOIN (SELECT
                                    person_id,
                                    max(obs_id) obs_id
                                  FROM obs
                                  WHERE concept_id = (SELECT concept_id
                                                      FROM concept_name
                                                      WHERE
                                                        name = 'Disposition' AND concept_name_type = 'FULLY_SPECIFIED')
                                  GROUP BY person_id) maxObsId ON maxObsId.person_id = bpam.patient_id
                      INNER JOIN obs latestDisposition
                        ON maxObsId.obs_id = latestDisposition.obs_id AND latestDisposition.voided = 0
                      INNER JOIN concept_name ON latestDisposition.value_coded = concept_name.concept_id AND
                                                 concept_name_type = 'FULLY_SPECIFIED'
                      LEFT OUTER JOIN encounter_provider ep ON latestDisposition.encounter_id = ep.encounter_id
                      LEFT OUTER JOIN provider disp_provider ON disp_provider.provider_id = ep.provider_id
                      LEFT OUTER JOIN person_name ON person_name.person_id = disp_provider.person_id
                    WHERE bpam.date_stopped IS NULL
                  ) dispositionInfo ON pv.person_id = dispositionInfo.person_id
  LEFT OUTER JOIN (
                    SELECT
                      diagnosis.person_id AS person_id,
                      diagnosis.obs_id AS obs_id,
                      diagnosis.obs_datetime AS diagnosis_datetime,
                      if(diagnosisConceptName.name IS NOT NULL, diagnosisConceptName.name,
                         diagnosis.value_text) AS diagnosisConcept,
                      certaintyConceptName.name AS certainty,
                      diagnosisOrderConceptName.name AS diagnosisOrder,
                      diagnosisStatusConceptName.name AS status,
                      person_name.given_name AS diagnosis_provider
                    FROM bed_patient_assignment_map bpam
                      INNER JOIN visit latestVisit
                        ON latestVisit.patient_id = bpam.patient_id AND latestVisit.date_stopped IS NULL AND
                           bpam.date_stopped IS NULL
                      INNER JOIN encounter ON encounter.visit_id = latestVisit.visit_id
                      INNER JOIN obs diagnosis ON bpam.patient_id = diagnosis.person_id AND diagnosis.voided = 0 AND
                                                  diagnosis.encounter_id = encounter.encounter_id AND
                                                  diagnosis.concept_id IN (SELECT concept_id
                                                                           FROM concept_name
                                                                           WHERE name IN
                                                                                 ('Coded Diagnosis', 'Non-Coded Diagnosis')
                                                                                 AND
                                                                                 concept_name_type = 'FULLY_SPECIFIED')
                      LEFT OUTER JOIN concept_name diagnosisConceptName
                        ON diagnosis.value_coded IS NOT NULL AND diagnosis.value_coded = diagnosisConceptName.concept_id
                           AND diagnosisConceptName.concept_name_type = 'FULLY_SPECIFIED'
                      LEFT OUTER JOIN encounter_provider ep ON diagnosis.encounter_id = ep.encounter_id
                      LEFT OUTER JOIN provider diagnosis_provider ON diagnosis_provider.provider_id = ep.provider_id
                      LEFT OUTER JOIN person_name ON person_name.person_id = diagnosis_provider.person_id
                      INNER JOIN obs certainty
                        ON diagnosis.obs_group_id = certainty.obs_group_id AND certainty.voided = 0 AND
                           certainty.concept_id = (SELECT concept_id
                                                   FROM concept_name
                                                   WHERE name = 'Diagnosis Certainty' AND
                                                         concept_name_type = 'FULLY_SPECIFIED')
                      LEFT OUTER JOIN concept_name certaintyConceptName
                        ON certainty.value_coded IS NOT NULL AND certainty.value_coded = certaintyConceptName.concept_id
                           AND certaintyConceptName.concept_name_type = 'FULLY_SPECIFIED'
                      INNER JOIN obs diagnosisOrder
                        ON diagnosis.obs_group_id = diagnosisOrder.obs_group_id AND diagnosisOrder.voided = 0 AND
                           diagnosisOrder.concept_id = (SELECT concept_id
                                                        FROM concept_name
                                                        WHERE name = 'Diagnosis order' AND
                                                              concept_name_type = 'FULLY_SPECIFIED')
                      LEFT OUTER JOIN concept_name diagnosisOrderConceptName ON diagnosisOrder.value_coded IS NOT NULL
                                                                                AND diagnosisOrder.value_coded =
                                                                                    diagnosisOrderConceptName.concept_id
                                                                                AND
                                                                                diagnosisOrderConceptName.concept_name_type
                                                                                = 'FULLY_SPECIFIED'
                      LEFT JOIN obs diagnosisStatus
                        ON diagnosis.obs_group_id = diagnosisStatus.obs_group_id AND diagnosisStatus.voided = 0 AND
                           diagnosisStatus.concept_id = (SELECT concept_id
                                                         FROM concept_name
                                                         WHERE name = 'Bahmni Diagnosis Status' AND
                                                               concept_name_type = 'FULLY_SPECIFIED')
                      LEFT OUTER JOIN concept_name diagnosisStatusConceptName ON diagnosisStatus.value_coded IS NOT NULL
                                                                                 AND diagnosisStatus.value_coded =
                                                                                     diagnosisStatusConceptName.concept_id
                                                                                 AND
                                                                                 diagnosisStatusConceptName.concept_name_type
                                                                                 = 'FULLY_SPECIFIED'
                  ) diagnosis ON diagnosis.person_id = pv.person_id",
'Sql query to get list of wards',
uuid()
);
