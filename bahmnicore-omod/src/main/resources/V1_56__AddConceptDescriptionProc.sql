CREATE PROCEDURE add_concept_description (concept_id INT,
                              description VARCHAR(250))
BEGIN
	INSERT INTO concept_description(uuid, concept_id, description, locale, creator, date_created) values(uuid(), concept_id, description, 'en', 1, now());
END;