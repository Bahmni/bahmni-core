-- In OpenMRS Core liquibase-core-data.xml the ids are hard coded ( 1 to 7)
SET foreign_key_checks = 0;
delete from person_attribute_type where person_attribute_type_id >= 1 and person_attribute_type_id <= 7;
SET foreign_key_checks = 1;
