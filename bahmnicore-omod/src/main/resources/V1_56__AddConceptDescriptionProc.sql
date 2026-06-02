-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

CREATE PROCEDURE add_concept_description (concept_id INT,
                              description VARCHAR(250))
BEGIN
	INSERT INTO concept_description(uuid, concept_id, description, locale, creator, date_created) values(uuid(), concept_id, description, 'en', 1, now());
END;