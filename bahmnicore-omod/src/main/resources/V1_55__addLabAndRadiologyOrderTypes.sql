-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

INSERT INTO order_type (`name`,`description`,`creator`,`date_created`,`retired`,`retired_by`,`date_retired`,`retire_reason`,`uuid`,`java_class_name`)
VALUES ('Lab Order','An order for laboratory tests',1,NOW(),0,NULL,NULL,NULL,UUID(),'org.openmrs.Order');

INSERT INTO order_type (`name`,`description`,`creator`,`date_created`,`retired`,`retired_by`,`date_retired`,`retire_reason`,`uuid`,`java_class_name`)
VALUES ('Radiology Order','An order for radiology tests',1,NOW(),0,NULL,NULL,NULL,UUID(),'org.openmrs.Order');
