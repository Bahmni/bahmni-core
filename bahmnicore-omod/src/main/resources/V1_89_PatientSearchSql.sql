DELETE FROM global_property
WHERE property IN (
  'emrapi.sqlSearch.patientsHasPendingOrders'
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
