DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.activePatients'
);

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`) 
VALUES ('emrapi.sqlSearch.activePatients',
    'select distinct concat(pn.given_name," ", ifnull(pn.family_name,"")) as name,
	primaryIdentifier.identifier as identifier,
	IF(extraIdentifier.identifier IS NULL OR extraIdentifier.identifier = \'\', primaryIdentifier.identifier, extraIdentifier.identifier) as abhaIdentifier,
	concat("",p.uuid) as uuid,
	concat("",v.uuid) as activeVisitUuid,
	IF(va.value_reference = "Admitted", "true", "false") as hasBeenAdmitted 
from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0
        join person p on p.person_id = v.patient_id
		JOIN (SELECT pri.patient_id, pri.identifier 
			FROM patient_identifier pri 
				join patient_identifier_type pit on pri.identifier_type = pit.patient_identifier_type_id 
				join global_property gp on gp.property="bahmni.primaryIdentifierType" and gp.property_value=pit.uuid) primaryIdentifier 
		ON v.patient_id = primaryIdentifier.patient_id 
		left outer JOIN (SELECT ei.patient_id, ei.identifier 
			FROM patient_identifier ei 
				join patient_identifier_type pit on ei.identifier_type = pit.patient_identifier_type_id 
				join global_property gp on gp.property="bahmni.extraPatientIdentifierTypes" and gp.property_value=pit.uuid) extraIdentifier 
		ON v.patient_id = extraIdentifier.patient_id 
        join location l on l.uuid = ${visit_location_uuid} and v.location_id = l.location_id
        left outer join visit_attribute va on va.visit_id = v.visit_id and va.attribute_type_id = (
          select visit_attribute_type_id from visit_attribute_type where name="Admission Status") and va.voided = 0 
where v.date_stopped is null AND v.voided = 0;',
        'Sql query to get list of active patients',
        uuid()
);
