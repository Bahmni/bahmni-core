-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

INSERT INTO scheduler_task_config(name, schedulable_class, start_time, start_time_pattern, repeat_interval, start_on_startup, started, created_by, date_created, uuid)
VALUES ('OpenElis Atom Feed Client', 'org.bahmni.module.elisatomfeedclient.api.task.OpenElisAtomFeedTask', now(), 'MM/dd/yyyy HH:mm:ss', 60, 1, 1, 1,  curdate(), uuid());