DELETE FROM global_property where property = 'emrapi.sqlGet.wardsListDetails';

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlGet.wardsListDetails',
"SELECT
  DISTINCT b.bed_number AS Bed, 
  p.uuid AS 'Patient Uuid',
  pi.identifier  AS 'Id',
  concat(pn.given_name, ' ', ifnull(pn.family_name,'')) AS 'Name',
  p.gender AS 'Gender', 
  TIMESTAMPDIFF(YEAR, p.birthdate, CURDATE()) AS 'Age',
  pa.county_district AS 'District', 
  pa.city_village AS 'Village',
  cast(DATE_FORMAT(admissionEncounter.encounter_datetime, '%d %b %y %h:%i %p') AS CHAR) AS 'Admission Time',
  adtNotesObs.value_text AS 'ADT Notes',
  admissionVisit.uuid AS 'Visit Uuid',
  concat(admProvName.given_name, ' ', ifnull(admProvName.family_name,'')) AS 'Admission By',
  dispositionInfo.disp_provider_name AS 'Disposition By',
  cast(DATE_FORMAT(dispositionInfo.disposition_date, '%d %b %y %h:%i %p') AS CHAR) AS 'Disposition Time',
  patient_diagnosis.encounter_diagnosis as 'Diagnosis',
  patient_diagnosis.diagnosis_by as 'Diagnosis Provider',
  cast(DATE_FORMAT(patient_diagnosis.diagnosis_datetime, '%d %b %y %h:%i %p') AS CHAR) AS 'Diagnosis Datetime'
FROM bed_location_map blm
INNER JOIN bed b ON blm.bed_id = b.bed_id AND b.status = 'OCCUPIED'
INNER JOIN bed_patient_assignment_map bpam ON b.bed_id = bpam.bed_id AND bpam.date_stopped IS NULL
INNER JOIN person p ON bpam.patient_id=p.person_id
INNER JOIN person_name pn ON pn.person_id = p.person_id
INNER JOIN patient_identifier pi ON pi.patient_id = bpam.patient_id AND pi.identifier_type= ${patient_identifier_type_id}
INNER JOIN location bl ON blm.location_id=bl.location_id
LEFT OUTER JOIN person_address pa ON pa.person_id = p.person_id
LEFT OUTER JOIN (
   SELECT DISTINCT
      do.person_id as patient_id, 
      concat(depn.given_name, ' ', ifnull(depn.family_name,'')) AS diagnosis_by,
      MAX(do.obs_datetime) as diagnosis_datetime,
      MAX(CASE WHEN do.concept_id = ${coded_diagnosis_concept_id} THEN dcn.name WHEN do.concept_id = ${non_coded_diagnosis_concept_id} THEN do.value_text END) as encounter_diagnosis  
   FROM obs do
   INNER JOIN (SELECT
      od.person_id AS patient_id, MAX(od.encounter_id) AS diagnosis_encounter_id 
      FROM bed_patient_assignment_map pam
      INNER JOIN bed ab ON pam.bed_id=ab.bed_id AND ab.status = 'OCCUPIED'
      INNER JOIN obs od ON pam.patient_id=od.person_id
      INNER JOIN encounter de ON od.encounter_id = de.encounter_id 
      WHERE od.concept_id=${visit_diagnoses_concept_id} GROUP BY od.person_id
   ) denc ON denc.patient_id = do.person_id AND do.encounter_id = denc.diagnosis_encounter_id
   LEFT OUTER JOIN concept_name dcn ON do.value_coded = dcn.concept_id AND dcn.concept_name_type='FULLY_SPECIFIED' AND dcn.locale='en'
   INNER JOIN encounter_provider dep ON dep.encounter_id = denc.diagnosis_encounter_id 
   INNER JOIN provider dp ON dp.provider_id = dep.provider_id
   LEFT OUTER JOIN person_name depn ON depn.person_id = dp.person_id
   GROUP BY patient_id, diagnosis_by
) patient_diagnosis ON patient_diagnosis.patient_id = bpam.patient_id
LEFT OUTER JOIN (SELECT
      bpame.patient_id,
      max(e.encounter_datetime) AS encounter_datetime,
      max(e.visit_id) as visit_id,
      max(e.encounter_id) AS encounter_id
      FROM bed_patient_assignment_map bpame
      INNER JOIN bed b ON bpame.bed_id=b.bed_id AND b.status = 'OCCUPIED'
      INNER JOIN bed_location_map bm ON bm.bed_id=b.bed_id
      INNER JOIN location loc ON bm.location_id=loc.location_id
      INNER JOIN encounter e ON e.patient_id=bpame.patient_id
      INNER JOIN encounter_type et ON e.encounter_type=et.encounter_type_id AND et.name='ADMISSION'
      INNER JOIN visit av ON av.visit_id=e.visit_id
      GROUP BY bpame.patient_id
) admissionEncounter ON admissionEncounter.patient_id=bpam.patient_id
LEFT OUTER JOIN visit admissionVisit ON admissionVisit.visit_id=admissionEncounter.visit_id
LEFT OUTER JOIN encounter_provider admEncProv ON admEncProv.encounter_id = admissionEncounter.encounter_id 
LEFT OUTER JOIN provider admProv ON admProv.provider_id = admEncProv.provider_id
LEFT OUTER JOIN person_name admProvName ON admProvName.person_id = admProv.person_id
LEFT OUTER JOIN obs adtNotesObs ON adtNotesObs.encounter_id = admissionEncounter.encounter_id AND adtNotesObs.concept_id=${adt_notes_concept_id}
LEFT OUTER JOIN (SELECT
      dpam.patient_id,
      dispCn.name AS disposition,
      lastDisp.obs_datetime AS disposition_date,
      concat(dispn.given_name, ' ', ifnull(dispn.family_name,'')) AS disp_provider_name
   FROM bed_patient_assignment_map dpam
   INNER JOIN (SELECT
      bpm.patient_id, MAX(dos.obs_id) AS obs_id 
      FROM bed_patient_assignment_map bpm
      INNER JOIN bed b ON bpm.bed_id=b.bed_id AND b.status = 'OCCUPIED'
      INNER JOIN obs dos ON dos.person_id=bpm.patient_id 
      WHERE dos.concept_id=${disposition_concept_id}
      GROUP BY bpm.patient_id
   ) maxObsId ON maxObsId.patient_id = dpam.patient_id
   INNER JOIN obs lastDisp ON maxObsId.obs_id = lastDisp.obs_id AND lastDisp.voided = 0
   INNER JOIN concept_name dispCn ON lastDisp.value_coded = dispCn.concept_id AND dispCn.concept_name_type = 'FULLY_SPECIFIED' and dispCn.locale='en'
   LEFT OUTER JOIN encounter_provider dispEp ON dispEp.encounter_id = lastDisp.encounter_id
   LEFT OUTER JOIN provider dispProv ON dispProv.provider_id = dispEp.provider_id
   LEFT OUTER JOIN person_name dispn ON dispn.person_id = dispProv.person_id
   WHERE dpam.date_stopped IS NULL
) dispositionInfo ON dispositionInfo.patient_id = bpam.patient_id
WHERE bl.name = ${location_name} OR bl.parent_location in (SELECT location_id FROM location WHERE name = ${location_name} )",
'Sql query to get list of wards',
uuid()
);
