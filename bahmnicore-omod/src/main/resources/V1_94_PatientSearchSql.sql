DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.activePatients',
  'emrapi.sqlSearch.activePatientsByProvider',
  'emrapi.sqlSearch.patientsToAdmit',
  'emrapi.sqlSearch.admittedPatients',
  'emrapi.sqlSearch.patientsToDischarge',
  'emrapi.sqlSearch.activePatientsByLocation',
  'emrapi.sqlSearch.highRiskPatients',
  'emrapi.sqlSearch.patientsHasPendingOrders'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatients',
        'select distinct
          concat(pn.given_name,\' \', pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
          select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
        ) and va.voided = 0
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of active patients',
        uuid()
);

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('emrapi.sqlSearch.activePatientsByProvider','
  select distinct concat(pn.given_name," ", pn.family_name) as name,
  pi.identifier as identifier,
  concat("",p.uuid) as uuid,
  concat("",v.uuid) as activeVisitUuid,
  IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
  from
    visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0
    join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided=0
    join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
    join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
    join person p on p.person_id = v.patient_id  and p.voided=0
    join encounter en on en.visit_id = v.visit_id and en.voided=0
    join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
    join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
    join person per on pr.person_id=per.person_id and per.voided=0
    join location l on l.uuid=${visit_location_uuid} and l.location_id = v.location_id
    left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id = (
				select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
			)
  where
    v.date_stopped is null and
    pr.uuid=${provider_uuid}
    order by en.encounter_datetime desc',
    'Sql query to get list of active patients by provider uuid',
  uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsToAdmit',
        'select distinct concat(pn.given_name,\' \', pn.family_name) as name,
        pi.identifier as identifier,
        concat("",p.uuid) as uuid,
        concat("",v.uuid) as activeVisitUuid
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND v.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on v.patient_id = p.person_id
        join encounter e on v.visit_id = e.visit_id
        join obs o on e.encounter_id = o.encounter_id and o.voided = 0
        join concept c on o.value_coded = c.concept_id
        join concept_name cn on c.concept_id = cn.concept_id
        join location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        where v.date_stopped is null and cn.name = \'Admit Patient\' and v.visit_id not in (select visit_id
        from encounter ie join encounter_type iet
        on iet.encounter_type_id = ie.encounter_type
        where iet.name = \'ADMISSION\')',
        'Sql query to get list of patients to be admitted',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.admittedPatients',
        'select distinct
          concat(pn.given_name," ", pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on v.patient_id = p.person_id
        join visit_attribute va on v.visit_id = va.visit_id and va.value_reference = "Admitted" and va.voided = 0
        join visit_attribute_type vat on vat.visit_attribute_type_id = va.attribute_type_id and vat.name = "Admission Status"
        join location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of admitted patients',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsToDischarge',
        'SELECT DISTINCT
          concat(pn.given_name, \' \', pn.family_name) AS name,
          pi.identifier AS identifier,
          concat("", p.uuid) AS uuid,
          concat("", v.uuid) AS activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        FROM visit v
        INNER JOIN person_name pn ON v.patient_id = pn.person_id and pn.voided is FALSE
        INNER JOIN patient_identifier pi ON v.patient_id = pi.patient_id and pi.voided is FALSE
        INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        INNER JOIN global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        INNER JOIN person p ON v.patient_id = p.person_id
        Inner Join (SELECT DISTINCT v.visit_id
          FROM encounter en
          INNER JOIN visit v ON v.visit_id = en.visit_id AND en.encounter_type =
            (SELECT encounter_type_id
              FROM encounter_type
            WHERE name = "ADMISSION")) v1 on v1.visit_id = v.visit_id
        INNER JOIN encounter e ON v.visit_id = e.visit_id
        INNER JOIN obs o ON e.encounter_id = o.encounter_id
        INNER JOIN concept_name cn ON o.value_coded = cn.concept_id AND cn.concept_name_type = "FULLY_SPECIFIED" AND cn.voided is FALSE
        JOIN location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
        LEFT OUTER JOIN encounter e1 ON e1.visit_id = v.visit_id AND e1.encounter_type = (
          SELECT encounter_type_id
            FROM encounter_type
          WHERE name = "DISCHARGE") AND e1.voided is FALSE
        WHERE v.date_stopped IS NULL AND v.voided = 0 AND o.voided = 0 AND cn.name = "Discharge Patient" AND e1.encounter_id IS NULL',
        'Sql query to get list of patients to discharge',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatientsByLocation',
        'select distinct concat(pn.given_name," ", pn.family_name) as name,
 pi.identifier as identifier,
 concat("",p.uuid) as uuid,
 concat("",v.uuid) as activeVisitUuid,
 IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
 from
   visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0
   join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided=0
   join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
   join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
   join person p on p.person_id = v.patient_id  and p.voided=0
   join encounter en on en.visit_id = v.visit_id and en.voided=0
   left outer join location loc on en.location_id = loc.location_id
   join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
   join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
   join person per on pr.person_id=per.person_id and per.voided=0
   left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
                select visit_attribute_type_id from visit_attribute_type where name="Admission Status"
            )
 where
   v.date_stopped is null and
   loc.uuid=${location_uuid}
   order by en.encounter_datetime desc',
        'SQL query to get list of active patients by location',
        uuid()
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
  INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
  INNER JOIN global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
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
        'SQL QUERY TO get LIST of patients with high risk',
        uuid()
);


INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.patientsHasPendingOrders',
        'select distinct
          concat(pn.given_name, " ", pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid,
          concat("",v.uuid) as activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="emr.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join orders on orders.patient_id = v.patient_id
        join order_type on orders.order_type_id = order_type.order_type_id and order_type.name != "Order" and order_type.name != "Drug Order"
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
        where v.date_stopped is null AND v.voided = 0 and order_id not in
          (select obs.order_id
            from obs
          where person_id = pn.person_id and order_id = orders.order_id)',
        'Sql query to get list of patients who has pending orders',
        uuid()
);


