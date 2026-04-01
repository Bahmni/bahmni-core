-- This Source Code Form is subject to the terms of the Mozilla Public License,
-- v. 2.0. If a copy of the MPL was not distributed with this file, You can
-- obtain one at https://www.bahmni.org/license/mplv2hd.
--
-- Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
-- graphic logo is a trademark of OpenMRS Inc.

-- This is a concept for concept-set
insert into concept (datatype_id, class_id, is_set, creator, date_created, changed_by, date_changed, uuid)
    values (1, 10, 0, 1, now(), 1, now(), uuid());
select max(concept_id) from concept into @registration_concepts_concept_id;
insert into concept_name (concept_id, name, locale, locale_preferred, creator, date_created, concept_name_type, uuid)
    values (@registration_concepts_concept_id, 'REGISTRATION_CONCEPTS', 'en', 1, 1, now(), 'FULLY_SPECIFIED', uuid());
