-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

CREATE PROCEDURE add_concept_numeric (concept_id INT,
							  low_normal DOUBLE,
							  hi_normal DOUBLE,
							  units VARCHAR(50))
BEGIN
  INSERT INTO concept_numeric (concept_id, low_normal, hi_normal, units) values (concept_id, low_normal, hi_normal, units);
END;

