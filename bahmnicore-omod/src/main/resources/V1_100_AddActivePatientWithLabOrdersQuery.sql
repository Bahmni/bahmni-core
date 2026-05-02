DELETE FROM global_property
WHERE property = 'emrapi.sqlSearch.activePatientsWithLabOrders';

INSERT INTO global_property (`property`, `property_value`, `description`, `uuid`)
VALUES ('emrapi.sqlSearch.activePatientsWithLabOrders',
        'select distinct
          concat(pn.given_name, " ", pn.family_name) as name,
          pi.identifier as identifier,
          concat("",p.uuid) as uuid
        from visit v
        join person_name pn on v.patient_id = pn.person_id and pn.voided = 0
        join patient_identifier pi on v.patient_id = pi.patient_id
        join patient_identifier_type pit on pi.identifier_type = pit.patient_identifier_type_id
        join global_property gp on gp.property="bahmni.primaryIdentifierType" and gp.property_value=pit.uuid
        join person p on p.person_id = v.patient_id
        join orders on orders.patient_id = v.patient_id
        join order_type on orders.order_type_id = order_type.order_type_id and order_type.name = "Lab Order"
        where v.date_stopped is null AND v.voided = 0 and order_id not in
          (select obs.order_id
            from obs
          where person_id = pn.person_id and order_id = orders.order_id)',
        'Sql query to get list of active patients with lab orders',
        uuid()
);
