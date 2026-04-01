-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

INSERT INTO patient_identifier_type (name, description, creator, date_created, required, uuid, location_behavior)
  VALUES ('JSS', 'New patient identifier type created for use by the Bahmni Registration System', 1, curdate(), 1, uuid(), 'NOT_USED');
