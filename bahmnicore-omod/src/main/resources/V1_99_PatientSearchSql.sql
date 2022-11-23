UPDATE global_property SET property_value = "select distinct
          concat(pn.given_name,' ', pn.family_name) as name,
          pi.identifier as identifier,
          concat('',p.uuid) as uuid,
          IF(va.value_reference = \"Admitted\", \"true\", \"false\") as hasBeenAdmitted
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and pn.preferred = 1
        join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided=0 and pi.preferred = 1
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property=\"bahmni.primaryIdentifierType\" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id and p.voided = 0
        join location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
          select visit_attribute_type_id from visit_attribute_type where name=\"Admission Status\"
        ) and va.voided = 0
        where v.date_stopped is null AND v.voided = 0;"
WHERE property = "emrapi.sqlSearch.activePatients";

UPDATE global_property SET property_value =
  "select distinct concat(pn.given_name,' ', pn.family_name) as name,
  pi.identifier as identifier,
  concat('',p.uuid) as uuid,
  IF(va.value_reference = \"Admitted\", \"true\", \"false\") as hasBeenAdmitted
  from
    visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0 and pn.preferred = 1
    join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided=0 and pi.preferred = 1
    join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
    join global_property gp on gp.property=\"bahmni.primaryIdentifierType\" and gp.property_value=pit.uuid
    join person p on p.person_id = v.patient_id  and p.voided=0
    join encounter en on en.visit_id = v.visit_id and en.voided=0
    join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
    join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
    join person per on pr.person_id=per.person_id and per.voided=0
    join location l on l.uuid=${visit_location_uuid} and l.location_id = v.location_id
    left outer join visit_attribute va on va.visit_id = v.visit_id and va.voided = 0 and va.attribute_type_id = (
				select visit_attribute_type_id from visit_attribute_type where name=\"Admission Status\"
			)
  where
    v.date_stopped is null and
    pr.uuid=${provider_uuid}
    order by en.encounter_datetime desc"
WHERE property = "emrapi.sqlSearch.activePatientsByProvider";

UPDATE global_property SET property_value =
 "select distinct concat(pn.given_name,' ', pn.family_name) as name,
 pi.identifier as identifier,
 concat('',p.uuid) as uuid,
 IF(va.value_reference = \"Admitted\", \"true\", \"false\") as hasBeenAdmitted
 from
   visit v join person_name pn on v.patient_id = pn.person_id and pn.voided = 0 and v.voided=0 and pn.preferred = 1
   join patient_identifier pi on v.patient_id = pi.patient_id and pi.voided=0 and pi.preferred = 1
   join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
   join global_property gp on gp.property=\"bahmni.primaryIdentifierType\" and gp.property_value=pit.uuid
   join person p on p.person_id = v.patient_id  and p.voided=0
   join encounter en on en.visit_id = v.visit_id and en.voided=0
   left outer join location loc on en.location_id = loc.location_id
   join encounter_provider ep on ep.encounter_id = en.encounter_id  and ep.voided=0
   join provider pr on ep.provider_id=pr.provider_id and pr.retired=0
   join person per on pr.person_id=per.person_id and per.voided=0
   left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
                select visit_attribute_type_id from visit_attribute_type where name=\"Admission Status\"
            )
 where
   v.date_stopped is null and
   loc.uuid=${location_uuid}
   order by en.encounter_datetime desc"
WHERE property = "emrapi.sqlSearch.activePatientsByLocation";