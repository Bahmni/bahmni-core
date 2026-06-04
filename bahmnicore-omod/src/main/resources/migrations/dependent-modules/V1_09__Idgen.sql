CREATE TABLE `idgen_identifier_source` (
  `id` int(11) NOT NULL auto_increment,
  `uuid` char(38) NOT NULL,
  `name` varchar(255) NOT NULL,
  `description` varchar(1000),
  `identifier_type` int(11) NOT NULL default '0',
  `creator` int(11) NOT NULL default '0',
  `date_created` datetime NOT NULL default '0000-00-00 00:00:00',
  `changed_by` int(11) default NULL,
	  `date_changed` datetime default NULL,
  `retired` tinyint(1) NOT NULL default 0,
  `retired_by` int(11) default NULL,
	  `date_retired` datetime default NULL,
	  `retire_reason` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `id for idgen_identifier_source` (`id`),
  KEY `identifier_type for idgen_identifier_source` (`identifier_type`),
  KEY `creator for idgen_identifier_source` (`creator`),
  KEY `changed_by for idgen_identifier_source` (`changed_by`),
  KEY `retired_by for idgen_identifier_source` (`retired_by`),
  CONSTRAINT `identifier_type for idgen_identifier_source` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
  CONSTRAINT `creator for idgen_identifier_source` FOREIGN KEY (`creator`) REFERENCES `users` (`user_id`),
  CONSTRAINT `changed_by for idgen_identifier_source` FOREIGN KEY (`changed_by`) REFERENCES `users` (`user_id`),
  CONSTRAINT `retired_by for idgen_identifier_source` FOREIGN KEY (`retired_by`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `idgen_seq_id_gen` (
  `id` int(11) NOT NULL,
  `next_sequence_value` int(11) NOT NULL default -1,
  `base_character_set` varchar(255) NOT NULL,
  `first_identifier_base` varchar(50) NOT NULL,
  `prefix` varchar(20),
  `suffix` varchar(20),
  `length` int(11),
  PRIMARY KEY  (`id`),
  CONSTRAINT `id for idgen_seq_id_gen` FOREIGN KEY (`id`) REFERENCES `idgen_identifier_source` (`id`)
);

CREATE TABLE `idgen_remote_source` (
  `id` int(11) NOT NULL,
  `url` varchar(255) NOT NULL,
  PRIMARY KEY  (`id`),
  CONSTRAINT `id for idgen_remote_source` FOREIGN KEY (`id`) REFERENCES `idgen_identifier_source` (`id`)
);

CREATE TABLE `idgen_id_pool` (
  `id` int(11) NOT NULL,
  `source` int(11),
  `batch_size` int(11),
  `min_pool_size` int(11),
  `sequential` tinyint(1) NOT NULL default 0,
  PRIMARY KEY  (`id`),
  KEY `source for idgen_id_pool` (`source`),
  CONSTRAINT `id for idgen_id_pool` FOREIGN KEY (`id`) REFERENCES `idgen_identifier_source` (`id`),
  CONSTRAINT `source for idgen_id_pool` FOREIGN KEY (`source`) REFERENCES `idgen_identifier_source` (`id`)
);

CREATE TABLE `idgen_pooled_identifier` (
  `id` int(11) NOT NULL auto_increment,
  `uuid` char(38) NOT NULL,
  `pool_id` int(11) NOT NULL,
  `identifier` varchar(50) NOT NULL,
  `date_used` datetime,
  `comment`  varchar(255),
  PRIMARY KEY  (`id`),
  CONSTRAINT `pool_id for idgen_pooled_identifier` FOREIGN KEY (`pool_id`) REFERENCES `idgen_id_pool` (`id`)
);

CREATE TABLE `idgen_auto_generation_option` (
  `id` int(11) NOT NULL auto_increment,
  `identifier_type` int(11) unique NOT NULL,
  `source` int(11) NOT NULL,
  `manual_entry_enabled` tinyint(1) NOT NULL default 1,
  `automatic_generation_enabled` tinyint(1) NOT NULL default 1,
  PRIMARY KEY  (`id`),
  CONSTRAINT `identifier_type for idgen_auto_generation_option` FOREIGN KEY (`identifier_type`) REFERENCES `patient_identifier_type` (`patient_identifier_type_id`),
  CONSTRAINT `source for idgen_auto_generation_option` FOREIGN KEY (`source`) REFERENCES `idgen_identifier_source` (`id`)
);

CREATE TABLE `idgen_log_entry` (
  `id` int(11) NOT NULL auto_increment,
  `source` int(11) NOT NULL,
  `identifier` varchar(50) NOT NULL,
  `date_generated` datetime NOT NULL default '0000-00-00 00:00:00',
  `generated_by` int(11) NOT NULL,
	  `comment` varchar(255) default NULL,
  PRIMARY KEY  (`id`),
  KEY `id for idgen_log` (`id`),
  KEY `source for idgen_log` (`source`),
  KEY `generated_by for idgen_log` (`generated_by`),
  CONSTRAINT `source for idgen_log` FOREIGN KEY (`source`) REFERENCES `idgen_identifier_source` (`id`),
  CONSTRAINT `generated_by for idgen_log` FOREIGN KEY (`generated_by`) REFERENCES `users` (`user_id`)
);

CREATE TABLE `idgen_reserved_identifier` (
  `id` int(11) NOT NULL auto_increment,
  `source` int(11) NOT NULL,
  `identifier` varchar(50) NOT NULL,
  PRIMARY KEY  (`id`),
  KEY `id for idgen_reserved_identifier` (`id`),
  KEY `source for idgen_reserved_identifier` (`source`),
  CONSTRAINT `source for idgen_reserved_identifier` FOREIGN KEY (`source`) REFERENCES `idgen_identifier_source` (`id`)
);

ALTER TABLE `idgen_id_pool` ADD COLUMN `refill_with_scheduled_task` tinyint(1) NOT NULL default 1;

ALTER TABLE `idgen_remote_source` ADD COLUMN `user` varchar(50) ;
ALTER TABLE `idgen_remote_source` ADD COLUMN `password` varchar(20) ;

insert into global_property (`property`, `property_value`, `description`, `uuid`) values ('idgen.database_version', '2.4.1',
'DO NOT MODIFY.  Current database version number for the idgen module.', uuid());