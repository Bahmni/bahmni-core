CREATE PROCEDURE add_concept_reference_map (concept_id INT,
							  concept_source_id INT,
							  reference_term VARCHAR(255),
                              reference_type_id INT)
BEGIN
  DECLARE reference_term_id INT;

    INSERT INTO concept_reference_term (concept_source_id,code,creator,date_created,uuid)
    VALUES (concept_source_id,reference_term,1,now(),uuid());
    SELECT MAX(concept_reference_term_id) INTO reference_term_id FROM concept_reference_term;

  INSERT INTO concept_reference_map(concept_reference_term_id,concept_map_type_id,creator,date_created,concept_id,uuid)
  VALUES(reference_term_id, reference_type_id, 1, now(), concept_id, uuid());

END;



