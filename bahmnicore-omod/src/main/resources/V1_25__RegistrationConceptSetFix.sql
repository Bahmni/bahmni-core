-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

select concept.concept_id from concept, concept_name where concept_name.concept_id = concept.concept_id and concept_name.name = 'REGISTRATION_CONCEPTS' into @concept_id;
update concept set is_set = 1 where concept_id = @concept_id;
delete from concept_set where concept_id = concept_set;