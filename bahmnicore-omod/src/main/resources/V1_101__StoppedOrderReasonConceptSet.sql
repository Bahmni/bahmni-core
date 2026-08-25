-- BAH-4689: Migrate "Stopped Order Reason" concept to a set and add "Refused To Take" as a member

SET @stoppedOrderReasonConceptId := (
  SELECT concept_id FROM concept_name WHERE name = 'Stopped Order Reason' AND concept_name_type = 'FULLY_SPECIFIED' LIMIT 1
);

UPDATE concept SET is_set = 1 WHERE concept_id = @stoppedOrderReasonConceptId AND is_set = 0;

SET @refusedToTakeConceptId := (
  SELECT concept_id FROM concept_name WHERE name = 'Refused To Take' AND concept_name_type = 'FULLY_SPECIFIED' LIMIT 1
);

-- Create the "Refused To Take" concept if it does not already exist
INSERT INTO concept (datatype_id, class_id, is_set, creator, date_created, uuid)
SELECT dt.concept_datatype_id, cc.concept_class_id, 0, 1, now(), uuid()
FROM concept_datatype dt, concept_class cc
WHERE dt.name = 'N/A' AND cc.name = 'Misc'
  AND @refusedToTakeConceptId IS NULL;

SET @refusedToTakeConceptId := IFNULL(
    @refusedToTakeConceptId,
    IF(ROW_COUNT() > 0, LAST_INSERT_ID(), NULL)
);

INSERT INTO concept_name (concept_id, name, locale, locale_preferred, creator, date_created, concept_name_type, uuid)
SELECT @refusedToTakeConceptId, 'Refused To Take', 'en', 1, 1, now(), 'FULLY_SPECIFIED', uuid()
WHERE @refusedToTakeConceptId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM concept_name WHERE concept_id = @refusedToTakeConceptId AND concept_name_type = 'FULLY_SPECIFIED'
  );

-- Add "Refused To Take" as a member of the "Stopped Order Reason" set if not already linked
INSERT INTO concept_set (concept_id, concept_set, sort_weight, creator, date_created, uuid)
SELECT @refusedToTakeConceptId, @stoppedOrderReasonConceptId,
  IFNULL((SELECT MAX(sort_weight) FROM concept_set WHERE concept_set = @stoppedOrderReasonConceptId), 0) + 1,
  1, now(), uuid()
WHERE @stoppedOrderReasonConceptId IS NOT NULL
  AND @refusedToTakeConceptId IS NOT NULL
  AND NOT EXISTS (
    SELECT 1 FROM concept_set WHERE concept_id = @refusedToTakeConceptId AND concept_set = @stoppedOrderReasonConceptId
  );
