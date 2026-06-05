CREATE PROCEDURE add_concept_numeric (concept_id INT,
							  low_normal DOUBLE,
							  hi_normal DOUBLE,
							  units VARCHAR(50))
BEGIN
  INSERT INTO concept_numeric (concept_id, low_normal, hi_normal, units) values (concept_id, low_normal, hi_normal, units);
END;

