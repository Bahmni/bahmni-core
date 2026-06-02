-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

UPDATE scheduler_task_config SET 
    schedulable_class="org.bahmni.module.elisatomfeedclient.api.task.OpenElisPatientFeedTask",
    name = "OpenElis Patient Atom Feed Task" 
where schedulable_class = "org.bahmni.module.elisatomfeedclient.api.task.OpenElisAtomFeedTask";

INSERT INTO scheduler_task_config(name, schedulable_class, start_time, start_time_pattern, repeat_interval, start_on_startup, started, created_by, date_created, uuid)
VALUES ('OpenElis Lab Result Atom Feed Task', 'org.bahmni.module.elisatomfeedclient.api.task.OpenElisLabResultFeedTask', now(), 'MM/dd/yyyy HH:mm:ss', 15, 1, 1, 1,  curdate(), uuid());