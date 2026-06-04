DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.patientsToDischarge',
  'emrapi.sqlSearch.patientsHasPendingOrders',
  'emrapi.sqlSearch.admittedPatients',
  'emrapi.sqlSearch.highRiskPatients',
  'emrapi.sqlSearch.activePatientsByProvider'
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
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id =
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
        join person p on p.person_id = v.patient_id
        join orders on orders.patient_id = v.patient_id
        join order_type on orders.order_type_id = order_type.order_type_id and order_type.name != "Lab Order" and order_type.name != "Drug Order"
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
        where v.date_stopped is null AND v.voided = 0 and order_id not in
          (select obs.order_id
            from obs
          where person_id = pn.person_id and order_id = orders.order_id)',
        'Sql query to get list of patients who has pending orders',
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
        join person p on v.patient_id = p.person_id
        join visit_attribute va on v.visit_id = va.visit_id and va.value_reference = "Admitted" and va.voided = 0
        join visit_attribute_type vat on vat.visit_attribute_type_id = va.attribute_type_id and vat.name = "Admission Status"
        where v.date_stopped is null AND v.voided = 0',
        'Sql query to get list of admitted patients',
        uuid()
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.highRiskPatients',
        'SELECT DISTINCT
         concat(pn.given_name, \' \', pn.family_name) AS name,
         pi.identifier                              AS identifier,
         concat("", p.uuid)                         AS uuid,
         concat("", v.uuid)                         AS activeVisitUuid,
         IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
         from person p
         inner join person_name pn on pn.person_id = p.person_id
         inner join patient_identifier pi ON pn.person_id = pi.patient_id
         inner join visit v on v.patient_id = p.person_id and v.date_stopped IS NULL and v.voided=0
         inner join concept_name cn on cn.name = \'LAB_ABNORMAL\' AND cn.concept_name_type = \'FULLY_SPECIFIED\'
         inner join obs o on o.voided=0 and o.value_coded=1 and o.person_id = p.person_id and o.concept_id = cn.concept_id
         left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id =
            (select visit_attribute_type_id from visit_attribute_type where name="Admission Status")
         where o.obs_group_id in (select obs_id from obs where voided=0 and concept_id IN (SELECT concept_id FROM concept_name cn WHERE cn.concept_name_type = \'FULLY_SPECIFIED\' AND ',
        'Sql query to get list of admitted patients',
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
    join person p on p.person_id = v.patient_id  and p.voided=0
    join encounter en on en.visit_id = v.visit_id and en.voided=0
    join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
    join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
    join person per on pr.person_id=per.person_id and per.voided=0
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
