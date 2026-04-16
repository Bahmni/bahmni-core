-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

CREATE PROCEDURE add_concept_answer (concept_id INT,
                              answer_concept_id INT,
                              sort_weight DOUBLE)
BEGIN
	INSERT INTO concept_answer (concept_id, answer_concept, answer_drug, date_created, creator, uuid, sort_weight) values (concept_id, answer_concept_id, null, now(), 1, uuid(), sort_weight);
END;