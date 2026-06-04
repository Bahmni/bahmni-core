CREATE TABLE IF NOT EXISTS address_hierarchy_type
(
    location_attribute_type_id int(11) NOT NULL auto_increment,
    name varchar(160) NOT NULL,
    PRIMARY KEY (location_attribute_type_id)
);

CREATE TABLE IF NOT EXISTS address_hierarchy
(
    location_attribute_type_value_id int(11) NOT NULL auto_increment,
    name varchar(160) NOT NULL,
    type_id int(11) NOT NULL,
    parent_location_attribute_type_value_id int(11),
    PRIMARY KEY (`location_attribute_type_value_id`),
    KEY `parent_location_id` (`parent_location_attribute_type_value_id`),
    KEY `location_type_id` (`type_id`),
    CONSTRAINT `parent_location_id` FOREIGN KEY (`parent_location_attribute_type_value_id`) REFERENCES `address_hierarchy` (`location_attribute_type_value_id`),
    CONSTRAINT `location_type_id` FOREIGN KEY (`type_id`) REFERENCES `address_hierarchy_type` (`location_attribute_type_id`)
);

ALTER TABLE address_hierarchy ADD COLUMN user_generated_id varchar(11);
ALTER TABLE address_hierarchy DROP FOREIGN KEY location_type_id;
ALTER TABLE address_hierarchy DROP KEY location_type_id;
ALTER TABLE address_hierarchy DROP FOREIGN KEY parent_location_id;
ALTER TABLE address_hierarchy DROP KEY parent_location_id;

ALTER TABLE address_hierarchy CHANGE COLUMN location_attribute_type_value_id `address_hierarchy_id` int(11) NOT NULL auto_increment;
ALTER TABLE address_hierarchy CHANGE COLUMN parent_location_attribute_type_value_id `parent_id` int(11);

ALTER TABLE address_hierarchy ADD KEY `parent_location_id` (`parent_id`);
ALTER TABLE address_hierarchy ADD CONSTRAINT `parent_location_id` FOREIGN KEY (`parent_id`) REFERENCES `address_hierarchy` (`address_hierarchy_id`);
ALTER TABLE address_hierarchy ADD KEY `location_type_id` (`type_id`);
ALTER TABLE address_hierarchy ADD CONSTRAINT `location_type_id` FOREIGN KEY (`type_id`) REFERENCES `address_hierarchy_type` (`location_attribute_type_id`);

ALTER TABLE address_hierarchy_type ADD COLUMN `parent_type_id` int(11) default NULL;
ALTER TABLE address_hierarchy_type ADD COLUMN `child_type_id` int(11) default NULL;

DROP TABLE IF EXISTS unstructured_address;
ALTER TABLE address_hierarchy add column latitude double, add column longitude double, add column elevation double;

create index name_ah on address_hierarchy(name);

ALTER TABLE address_hierarchy DROP FOREIGN KEY location_type_id;
ALTER TABLE address_hierarchy_type CHANGE COLUMN `location_attribute_type_id` `address_hierarchy_type_id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE address_hierarchy_type ADD COLUMN `address_field` varchar(50) NOT NULL;

ALTER TABLE address_hierarchy
ADD COLUMN `uuid` char(38)
NOT NULL;

ALTER TABLE address_hierarchy_type
ADD COLUMN `uuid` char(38)
NOT NULL;

UPDATE address_hierarchy SET uuid = UUID();
UPDATE address_hierarchy_type SET uuid = UUID();

ALTER TABLE address_hierarchy_type DROP COLUMN child_type_id;

ALTER TABLE address_hierarchy RENAME address_hierarchy_entry;

ALTER TABLE address_hierarchy_entry DROP FOREIGN KEY parent_location_id;
ALTER TABLE address_hierarchy_entry CHANGE COLUMN `address_hierarchy_id` `address_hierarchy_entry_id` int(11) NOT NULL AUTO_INCREMENT;

ALTER TABLE address_hierarchy_type RENAME address_hierarchy_level;

ALTER TABLE address_hierarchy_level CHANGE COLUMN `address_hierarchy_type_id` `address_hierarchy_level_id` int(11) NOT NULL AUTO_INCREMENT;
ALTER TABLE address_hierarchy_level CHANGE COLUMN `parent_type_id` `parent_level_id` int(11);

ALTER TABLE address_hierarchy_entry CHANGE COLUMN `type_id` `level_id` int(11);

ALTER TABLE address_hierarchy_level CHANGE COLUMN `name` `name` varchar(160);

ALTER TABLE address_hierarchy_level CHANGE COLUMN `address_field` `address_field` varchar(50);

ALTER TABLE address_hierarchy_level ADD COLUMN `required` tinyint(1) NOT NULL default '0';

ALTER TABLE address_hierarchy_entry CHANGE COLUMN `level_id` `level_id` int(11) NOT NULL;

ALTER TABLE address_hierarchy_level ADD INDEX address_field_unique (address_field);
ALTER TABLE address_hierarchy_level ADD UNIQUE parent_level_id_unique (parent_level_id);

DROP INDEX parent_location_id ON address_hierarchy_entry;
DROP INDEX location_type_id ON address_hierarchy_entry;
DROP INDEX name_ah ON address_hierarchy_entry;
ALTER TABLE address_hierarchy_entry ADD INDEX parent_name (parent_id,name(20));
ALTER TABLE address_hierarchy_entry ADD INDEX level_name (level_id,name(20));

ALTER TABLE address_hierarchy_entry ADD CONSTRAINT parent_to_parent FOREIGN KEY (parent_id) REFERENCES address_hierarchy_entry (address_hierarchy_entry_id);
ALTER TABLE address_hierarchy_entry ADD CONSTRAINT level_to_level FOREIGN KEY (level_id) REFERENCES address_hierarchy_level (address_hierarchy_level_id);
ALTER TABLE address_hierarchy_level ADD CONSTRAINT parent_level FOREIGN KEY (parent_level_id) REFERENCES address_hierarchy_level (address_hierarchy_level_id);

ALTER TABLE address_hierarchy_entry DROP FOREIGN KEY parent_to_parent;
ALTER TABLE address_hierarchy_entry ADD CONSTRAINT `parent-to-parent` FOREIGN KEY (`parent_id`) REFERENCES `address_hierarchy_entry` (`address_hierarchy_entry_id`) ON DELETE CASCADE;

CREATE TABLE IF NOT EXISTS address_hierarchy_address_to_entry_map
(
    address_to_entry_map_id int(11) NOT NULL auto_increment,
    address_id int(11) NOT NULL,
    entry_id int(11) NOT NULL,
    uuid char(38) NOT NULL,
    PRIMARY KEY (address_to_entry_map_id),
    CONSTRAINT address_id_to_person_address_table FOREIGN KEY person_address_index (address_id) REFERENCES person_address (person_address_id),
    CONSTRAINT entry_id_to_address_hierarchy_table FOREIGN KEY address_hierarchy_index (entry_id) REFERENCES address_hierarchy_entry (address_hierarchy_entry_id)
);

-- Insert global properties
insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.addressToEntryMapUpdaterLastStartTime', NULL, 'The module uses this field to store when the AddressToEntryMapUpdater task was last started; DO NOT MODIFY', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.allowFreetext', 'true', 'Valid values: true/false. When overriding the address portlet, allow the entry of free text for address fields associated with the address hierarchy by providing an "Other" option', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.database_version', '2.8.0', 'DO NOT MODIFY.  Current database version number for the addresshierarchy module.', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.enableOverrideOfAddressPortlet', 'true', 'Valid values: true/false. When enabled, the existing "edit" component of the address portlet is overridden by the new functionality provided by the address hierarchy module', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.initializeAddressHierarchyCacheOnStartup', 'true', 'Sets whether to initialize the address hierarchy in-memory cache (which is used to speed up address hierarchy searches. Generally, you want to set this to "true", though developers may want to set it to false during development to speed module start-up.', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.mandatory', 'false', 'true/false whether or not the addresshierarchy module MUST start when openmrs starts.  This is used to make sure that mission critical modules are always running if openmrs is running.', uuid());

insert into global_property (`property`, `property_value`, `description`, `uuid`)
values ('addresshierarchy.soundexProcessor', NULL, 'If the Name Phonetics module is installed, this defines the name of a soundex algorithm used by the getPossibleFullAddresses service method.', uuid());