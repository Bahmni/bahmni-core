-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

select uuid from patient_identifier_type where name = 'Bahmni Id' into @uuid;
UPDATE global_property SET property_value = @uuid WHERE property='emr.primaryIdentifierType';
