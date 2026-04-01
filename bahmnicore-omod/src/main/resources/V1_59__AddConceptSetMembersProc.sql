-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

CREATE PROCEDURE add_concept_set_members (set_concept_id INT,
                              member_concept_id INT,weight INT)
BEGIN
	INSERT INTO concept_set (concept_id, concept_set,sort_weight,creator,date_created,uuid)
	values (member_concept_id, set_concept_id,weight,1, now(),uuid());
END;



