-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

-- In OpenMRS Core liquibase-core-data.xml the ids are hard coded ( 1 to 7)
SET foreign_key_checks = 0;
delete from person_attribute_type where person_attribute_type_id >= 1 and person_attribute_type_id <= 7;
SET foreign_key_checks = 1;
