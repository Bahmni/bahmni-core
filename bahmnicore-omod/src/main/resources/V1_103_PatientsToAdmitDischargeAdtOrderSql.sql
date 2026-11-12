DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.patientsToAdmit',
  'emrapi.sqlSearch.patientsToDischarge'
);

INSERT INTO global_property (property, property_value, description, uuid)
VALUES (
  'emrapi.sqlSearch.patientsToAdmit',
  'select distinct name, identifier, uuid, activeVisitUuid from (
  select concat(pn.given_name,'' '', ifnull(pn.family_name,'''')) as name,
        pi.identifier as identifier,
        concat("", p.uuid) as uuid,
        concat("", v.uuid) as activeVisitUuid
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 AND v.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided = 0
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="bahmni.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on v.patient_id = p.person_id
        join encounter e on v.visit_id = e.visit_id
        join obs o on e.encounter_id = o.encounter_id and o.voided = 0
        join concept c on o.value_coded = c.concept_id
        join concept_name cn on c.concept_id = cn.concept_id and cn.concept_name_type = "FULLY_SPECIFIED" and cn.voided = 0
        join location l on l.uuid=${visit_location_uuid} and v.location_id = l.location_id
        where v.date_stopped is null and cn.name in (''Admit Patient'', ''Undo Discharge'') and not exists (
          select 1 from encounter ae
          join encounter_type aet on ae.encounter_type = aet.encounter_type_id
          where ae.visit_id = v.visit_id and ae.voided = 0
            and aet.name in (''ADMISSION'', ''DISCHARGE'')
            and ae.encounter_datetime > o.obs_datetime
        )

  union

  select concat(pn2.given_name,'' '', ifnull(pn2.family_name,'''')) as name,
        pi2.identifier as identifier,
        concat("", p2.uuid) as uuid,
        concat("", v2.uuid) as activeVisitUuid
        from orders ord
        join order_type ot on ord.order_type_id = ot.order_type_id
        join concept c2 on ord.concept_id = c2.concept_id
        join concept_name cn2 on c2.concept_id = cn2.concept_id and cn2.concept_name_type = "FULLY_SPECIFIED" and cn2.voided = 0
        join encounter e2 on ord.encounter_id = e2.encounter_id
        join visit v2 on e2.visit_id = v2.visit_id
        join person p2 on v2.patient_id = p2.person_id
        join person_name pn2 on p2.person_id = pn2.person_id and pn2.voided = 0
        join patient_identifier pi2 on v2.patient_id = pi2.patient_id and pi2.voided = 0
        join patient_identifier_type pit2 on pi2.identifier_type = pit2.patient_identifier_type_id
        join global_property gp2 on gp2.property="bahmni.primaryIdentifierType" and gp2.property_value=pit2.uuid
        join location l2 on l2.uuid=${visit_location_uuid} and v2.location_id = l2.location_id
        where ot.name = ''ADT Order'' and cn2.name in (''Admit Patient'', ''Undo Discharge'') and ord.voided = 0 and v2.voided = 0
          AND ord.order_action <> ''DISCONTINUE'' AND ord.date_stopped IS NULL
          AND (ord.auto_expire_date IS NULL OR ord.auto_expire_date > now())
          and (
            (ord.scheduled_date is not null and date(ord.scheduled_date) >= date_sub(current_date(), interval (ifnull((select cast(gp3.property_value as signed) from global_property gp3 where gp3.property = ''bahmni.admitOrder.visibilityWindowDays''), 1) - 1) day))
            or (ord.scheduled_date is null and v2.date_stopped is null)
          )
          and not exists (
            select 1 from encounter ae2
            join encounter_type aet2 on ae2.encounter_type = aet2.encounter_type_id
            where ae2.visit_id = v2.visit_id and ae2.voided = 0
              and aet2.name in (''ADMISSION'', ''DISCHARGE'')
              and ae2.encounter_datetime > ifnull(ord.date_activated, ord.date_created)
          )
          and not (cn2.name = ''Undo Discharge'' and exists (
            select 1 from encounter ve
            join encounter_type vet on ve.encounter_type = vet.encounter_type_id
            where ve.visit_id = v2.visit_id and ve.voided = 1 and vet.name = ''DISCHARGE''
              and ve.date_voided > ifnull(ord.date_activated, ord.date_created)
          ))
          and not exists (
            select 1 from orders ord_later
            join order_type ot_later on ord_later.order_type_id = ot_later.order_type_id
            join encounter e_later on ord_later.encounter_id = e_later.encounter_id
            where e_later.visit_id = v2.visit_id and ord_later.voided = 0
              and ot_later.name = ''ADT Order''
              and ifnull(ord_later.date_activated, ord_later.date_created) > ifnull(ord.date_activated, ord.date_created)
          )
) combined',
  'Sql query to get list of patients to be admitted (observation-based legacy or ADT order-based)',
  uuid()
);

INSERT INTO global_property (property, property_value, description, uuid)
VALUES (
  'emrapi.sqlSearch.patientsToDischarge',
  'SELECT DISTINCT name, identifier, uuid, activeVisitUuid, hasBeenAdmitted FROM (
  SELECT
          concat(pn.given_name, '' '', ifnull(pn.family_name,'''')) AS name,
          pi.identifier AS identifier,
          concat("", p.uuid) AS uuid,
          concat("", v.uuid) AS activeVisitUuid,
          IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        FROM visit v
        INNER JOIN person_name pn ON v.patient_id = pn.person_id and pn.voided is FALSE
        INNER JOIN patient_identifier pi ON v.patient_id = pi.patient_id and pi.voided is FALSE
        INNER JOIN patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        INNER JOIN global_property gp on gp.property="bahmni.primaryIdentifierType" and gp.property_value=pit.uuid
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
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status") and va.voided = 0
        LEFT OUTER JOIN encounter e1 ON e1.visit_id = v.visit_id AND e1.encounter_type = (
          SELECT encounter_type_id
            FROM encounter_type
          WHERE name = "DISCHARGE") AND e1.voided is FALSE
        WHERE v.date_stopped IS NULL AND v.voided = 0 AND o.voided = 0 AND cn.name = "Discharge Patient" AND e1.encounter_id IS NULL

  UNION

  SELECT
          concat(pn2.given_name, '' '', ifnull(pn2.family_name,'''')) AS name,
          pi2.identifier AS identifier,
          concat("", p2.uuid) AS uuid,
          concat("", v2.uuid) AS activeVisitUuid,
          IF(va2.value_reference = "Admitted", "true", "false") as hasBeenAdmitted
        FROM orders ord
        JOIN order_type ot ON ord.order_type_id = ot.order_type_id
        JOIN concept c2 ON ord.concept_id = c2.concept_id
        JOIN concept_name cn2 ON c2.concept_id = cn2.concept_id AND cn2.concept_name_type = "FULLY_SPECIFIED" AND cn2.voided is FALSE
        JOIN encounter e2 ON ord.encounter_id = e2.encounter_id
        JOIN visit v2 ON e2.visit_id = v2.visit_id
        JOIN person p2 ON v2.patient_id = p2.person_id
        JOIN person_name pn2 ON p2.person_id = pn2.person_id AND pn2.voided = 0
        JOIN patient_identifier pi2 ON v2.patient_id = pi2.patient_id AND pi2.voided is FALSE
        JOIN patient_identifier_type pit2 ON pi2.identifier_type = pit2.patient_identifier_type_id
        JOIN global_property gp2 ON gp2.property="bahmni.primaryIdentifierType" and gp2.property_value=pit2.uuid
        JOIN location l2 ON l2.uuid=${visit_location_uuid} and v2.location_id = l2.location_id
        left outer join visit_attribute va2 on va2.visit_id = v2.visit_id and va2.attribute_type_id =
          (select visit_attribute_type_id from visit_attribute_type where name="Admission Status") and va2.voided = 0
        Inner Join (SELECT DISTINCT v.visit_id
          FROM encounter en
          INNER JOIN visit v ON v.visit_id = en.visit_id AND en.encounter_type =
            (SELECT encounter_type_id
              FROM encounter_type
            WHERE name = "ADMISSION")) v3 on v3.visit_id = v2.visit_id
        WHERE ot.name = ''ADT Order'' AND cn2.name = ''Discharge Patient'' AND ord.voided = 0
          AND ord.order_action <> ''DISCONTINUE'' AND ord.date_stopped IS NULL
          AND (ord.auto_expire_date IS NULL OR ord.auto_expire_date > now())
          AND v2.date_stopped IS NULL AND v2.voided = 0
          AND NOT EXISTS (
            SELECT 1 FROM encounter ae2
            JOIN encounter_type aet2 ON ae2.encounter_type = aet2.encounter_type_id
            WHERE ae2.visit_id = v2.visit_id AND ae2.voided = 0
              AND aet2.name = ''DISCHARGE''
              AND ae2.encounter_datetime > ifnull(ord.date_activated, ord.date_created)
          )
) combined',
  'Sql query to get list of patients to discharge (observation-based legacy or ADT order-based)',
  uuid()
);
