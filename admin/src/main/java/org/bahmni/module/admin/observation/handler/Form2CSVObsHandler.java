package org.bahmni.module.admin.observation.handler;

import org.bahmni.csv.KeyValue;
import org.bahmni.module.admin.csv.models.EncounterRow;
import org.bahmni.module.admin.csv.models.SectionPositionValue;
import org.bahmni.module.admin.csv.service.FormFieldPathGeneratorService;
import org.bahmni.module.admin.csv.utils.CSVUtils;
import org.bahmni.module.admin.observation.CSVObservationHelper;
import org.bahmni.form2.service.FormFieldPathService;
import org.openmrs.api.APIException;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.openmrs.module.webservices.rest.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Arrays.asList;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.bahmni.module.admin.observation.CSVObservationHelper.getLastItem;

@Component
public class Form2CSVObsHandler implements CSVObsHandler {

    private static final String DATE = "Date";
    private static final String ATTRIBUTE_QUERY_SEPARATOR = "?";
    private static final String ATTRIBUTE_SEPARATOR = "&";
    private static final String ATTRIBUTE_ISJSON = "isJson";
    private static final String SECTION_SPLITTER = "/";
    private static final String KEY_SECTION_VALUES = "values";
    private static final int NOT_MULTISELECT_OBS_INDEX = -1;
    private static final int NOT_ADDMORE_OBS_INDEX = -1;

    private CSVObservationHelper csvObservationHelper;
    private FormFieldPathService formFieldPathService;
    private FormFieldPathGeneratorService formFieldPathGeneratorService;

    @Autowired
    public Form2CSVObsHandler(CSVObservationHelper csvObservationHelper, FormFieldPathService formFieldPathService, FormFieldPathGeneratorService formFieldPathGeneratorService) {
        this.csvObservationHelper = csvObservationHelper;
        this.formFieldPathService = formFieldPathService;
        this.formFieldPathGeneratorService = formFieldPathGeneratorService;
    }

    @Override
    public List<KeyValue> getRelatedCSVObs(EncounterRow encounterRow) {
        return encounterRow.obsRows.stream().filter(csvObservation -> csvObservationHelper.isForm2Type(csvObservation))
                .collect(Collectors.toList());
    }

    @Override
    public List<EncounterTransaction.Observation> handle(EncounterRow encounterRow) throws ParseException {
        List<EncounterTransaction.Observation> form2Observations = new ArrayList<>();
        List<KeyValue> form2CSVObservations = getRelatedCSVObs(encounterRow);
        for (KeyValue form2CSVObservation : form2CSVObservations) {
            if (isNotBlank(form2CSVObservation.getValue())) {
                final List<String> form2CSVHeaderParts = getCSVHeaderPartsByIgnoringForm2KeyWord(form2CSVObservation);
                verifyCSVHeaderHasConcepts(form2CSVObservation, form2CSVHeaderParts);
                csvObservationHelper.verifyNumericConceptValue(form2CSVObservation, form2CSVHeaderParts);
                csvObservationHelper.createObservations(form2Observations, encounterRow.getEncounterDate(),
                        form2CSVObservation, getConceptNames(form2CSVHeaderParts));
                formFieldPathGeneratorService.setFormNamespaceAndFieldPath(form2Observations, form2CSVHeaderParts);
            }
        }
        return form2Observations;
    }

    @Override
    public List<EncounterTransaction.Observation> handle(EncounterRow encounterRow, boolean shouldPerformForm2Validations) throws ParseException {
        if(!shouldPerformForm2Validations)
            return handle(encounterRow);
        List<EncounterTransaction.Observation> form2Observations = new ArrayList<>();
        List<KeyValue> form2CSVObservations = getRelatedCSVObs(encounterRow);
        for (KeyValue form2CSVObservation : form2CSVObservations) {
            Map<String, Boolean> headerAttributes = parseCSVHeader(form2CSVObservation);
            boolean isJsonAttribute = headerAttributes.getOrDefault(ATTRIBUTE_ISJSON, false);
            final List<String> form2CSVHeaderParts = getCSVHeaderPartsByIgnoringForm2KeyWord(form2CSVObservation);
            final boolean validCSVHeader = formFieldPathService.isValidCSVHeader(form2CSVHeaderParts);
            if(!validCSVHeader)
                throw new APIException(format("No concepts found in %s", form2CSVObservation.getKey()));
            if (isNotBlank(form2CSVObservation.getValue())) {
                if(isJsonAttribute) {
                    processJsonConceptValue(form2CSVObservation, form2CSVHeaderParts, form2Observations, encounterRow);
                } else {
                    verifyCSVHeaderHasConcepts(form2CSVObservation, form2CSVHeaderParts);
                    csvObservationHelper.verifyNumericConceptValue(form2CSVObservation, form2CSVHeaderParts);
                    verifyForMultiSelect(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts);
                    verifyForAddMore(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts);
                    verifyAndValidateObs(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts);
                }
            } else {
                verifyForMandatoryObs(form2CSVHeaderParts);
            }
        }
        return form2Observations;
    }

    private void verifyCSVHeaderHasConcepts(KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) {
        if (form2CSVHeaderParts.size() <= 1) {
            throw new APIException(format("No concepts found in %s", form2CSVObservation.getKey()));
        }
    }

    private List<String> getCSVHeaderPartsByIgnoringForm2KeyWord(KeyValue csvObservation) {
        final List<String> csvHeaderParts = csvObservationHelper.getCSVHeaderParts(csvObservation);
        // removes form2 keyword
        csvHeaderParts.remove(0);
        return csvHeaderParts;
    }

    private List<String> getConceptNames(List<String> form2CSVHeaderParts) {
        return asList(getLastItem(form2CSVHeaderParts));
    }

    private void verifyForMultiSelect(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        boolean isMultiSelectObs = formFieldPathService.isMultiSelectObs(form2CSVHeaderParts);
        if(isMultiSelectObs) {
            processMultiSelectObs(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts);
        }
    }

    private void verifyForAddMore(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        boolean isAddmoreConceptObs = formFieldPathService.isAddmore(form2CSVHeaderParts);
        boolean isMultiSelectObs = formFieldPathService.isMultiSelectObs(form2CSVHeaderParts);
        if(!isMultiSelectObs && isAddmoreConceptObs) {
            processAddmoreConcept(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts);
        }
    }

    private void verifyForMandatoryObs(List<String> form2CSVHeaderParts) {
        boolean mandatoryFieldMissing = formFieldPathService.isMandatory(form2CSVHeaderParts);
        if(mandatoryFieldMissing) {
            throw new APIException(format("Empty value provided for mandatory field %s", form2CSVHeaderParts.get(form2CSVHeaderParts.size()-1)));
        }
    }

    private void verifyAndValidateObs(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        boolean isMultiSelectObs = formFieldPathService.isMultiSelectObs(form2CSVHeaderParts);
        boolean isAddmoreConceptObs = formFieldPathService.isAddmore(form2CSVHeaderParts);
        if(!isMultiSelectObs && !isAddmoreConceptObs) {
            csvObservationHelper.createObservations(form2Observations, encounterRow.getEncounterDate(),
                    form2CSVObservation, getConceptNames(form2CSVHeaderParts));
            formFieldPathGeneratorService.setFormNamespaceAndFieldPath(form2Observations, form2CSVHeaderParts);
            validateObsForFutureDate(form2Observations, form2CSVObservation, form2CSVHeaderParts);
        }
    }

    private void processMultiSelectObs(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        List<String> multiSelectValues = csvObservationHelper.getMultiSelectObs(form2CSVObservation);
        List<KeyValue> multiSelectCSVObservations = processMultipleValues(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts, multiSelectValues);
        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForMultiSelectObs(form2Observations, form2CSVHeaderParts, multiSelectCSVObservations);
    }

    private void processAddmoreConcept(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        List<String> multiSelectValues = csvObservationHelper.getAddmoreObs(form2CSVObservation);
        List<KeyValue> addmoreCSVObservations = processMultipleValues(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts, multiSelectValues);
        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForAddmoreObs(form2Observations, form2CSVHeaderParts, addmoreCSVObservations);
    }

    private List<KeyValue> processMultipleValues(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts, List<String> multipleValues) throws ParseException {
        List<KeyValue> form2CSVObservations = new ArrayList<>();
        for (String value : multipleValues) {
            KeyValue newForm2CSVObservation = new KeyValue();
            newForm2CSVObservation.setKey(form2CSVObservation.getKey());
            newForm2CSVObservation.setValue(value.trim());
            form2CSVObservations.add(newForm2CSVObservation);
        }
        csvObservationHelper.createObservations(form2Observations, encounterRow.getEncounterDate(),
                form2CSVObservations, getConceptNames(form2CSVHeaderParts));
        return form2CSVObservations;
    }

    private void validateObsForFutureDate(List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts) throws ParseException {
        EncounterTransaction.Observation observation = getLastItem(form2Observations);
        if(DATE.equals(observation.getConcept().getDataType())) {
            boolean isAllowFutureDates = formFieldPathService.isAllowFutureDates(form2CSVHeaderParts);
            if(!isAllowFutureDates) {
                Date todaysDate = CSVUtils.getTodayDate();
                if(todaysDate.before(CSVUtils.getDateFromString((String)observation.getValue()))) {
                    throw new APIException(format("Future date [%s] is not allowed for [%s]", form2CSVObservation.getValue(), form2CSVHeaderParts.get(form2CSVHeaderParts.size()-1)));
                }
            }
        }
    }

    private void processJsonConceptValue(KeyValue form2CSVObservation, List<String> form2CSVHeaderParts, List<EncounterTransaction.Observation> form2Observations, EncounterRow encounterRow) throws ParseException {
        SimpleObject jsonObject = parseJson(form2CSVObservation);
        List<SectionPositionValue> sectionPositionValues = getSectionPositions(form2CSVObservation, jsonObject, form2CSVHeaderParts);
        verifyCSVHeaderHasConcepts(form2CSVObservation, form2CSVHeaderParts);
        verifyAndValidateObsForJsonValue(encounterRow, form2Observations, form2CSVObservation, form2CSVHeaderParts, sectionPositionValues);
    }

    private void verifyAndValidateObsForJsonValue(EncounterRow encounterRow, List<EncounterTransaction.Observation> form2Observations, KeyValue form2CSVObservation, List<String> form2CSVHeaderParts, List<SectionPositionValue> sectionPositionValues) throws ParseException {
        List<KeyValue> form2CSVObservations = new ArrayList<>();
        sectionPositionValues.stream().forEach(sectionPositionValue -> {
            if(isNotBlank(sectionPositionValue.getValue())) {
                KeyValue newForm2CSVObservation = new KeyValue();
                newForm2CSVObservation.setKey(form2CSVObservation.getKey());
                newForm2CSVObservation.setValue(sectionPositionValue.getValue().trim());
                form2CSVObservations.add(newForm2CSVObservation);
            }
        });
        csvObservationHelper.createObservations(form2Observations, encounterRow.getEncounterDate(),
                form2CSVObservations, getConceptNames(form2CSVHeaderParts));
        formFieldPathGeneratorService.setFormNamespaceAndFieldPathForJsonValue(form2Observations, form2CSVHeaderParts, form2CSVObservations, sectionPositionValues);
    }

    private Map<String, Boolean> parseCSVHeader(KeyValue form2CSVObservation) {
        Map<String, Boolean> attributeMap = new HashMap<>();
        String header = form2CSVObservation.getKey();
        final int attributeStartIndex = header.lastIndexOf(ATTRIBUTE_QUERY_SEPARATOR);
        if(attributeStartIndex != -1) {
            final String[] attributes = header.substring(attributeStartIndex + 1).split(ATTRIBUTE_SEPARATOR);
            Arrays.stream(attributes).forEach(attribute -> {
                if(attribute.contains("=")) {
                    String attributeName = attribute.split("=")[0];
                    String attributeValue = attribute.split("=")[1];
                    attributeMap.put(attributeName, Boolean.valueOf(attributeValue));
                }
            });
            if(attributeMap.size() > 0)
                form2CSVObservation.setKey(header.substring(0, header.lastIndexOf(ATTRIBUTE_QUERY_SEPARATOR)));
        }
        return attributeMap;
    }

    private SimpleObject parseJson(KeyValue form2CSVObservation) {
        SimpleObject jsonObject;
        try {
            String jsonValueStr = form2CSVObservation.getValue();
            jsonObject = SimpleObject.parseJson(jsonValueStr);
        } catch (Exception e) {
            throw new APIException(format("Error in parsing json value for %s", form2CSVObservation.getKey()));
        }
        return jsonObject;
    }

    private List<SectionPositionValue> getSectionPositions(KeyValue form2CSVObservation, SimpleObject jsonObject, List<String> form2CSVHeaderParts) {
        List<SectionPositionValue> sectionPositionValues = new ArrayList<>();
        String initialSectionIndex = "0";
        Object objectValue = jsonObject.get(KEY_SECTION_VALUES);
        if(objectValue == null)
            throw new APIException(format("Error in parsing json value for %s", form2CSVObservation.getKey()));

        boolean isAddmoreConceptObs = formFieldPathService.isAddmore(form2CSVHeaderParts);
        boolean isMultiSelectObs = formFieldPathService.isMultiSelectObs(form2CSVHeaderParts);

        updateSectionPositionValues(sectionPositionValues, objectValue, initialSectionIndex, isMultiSelectObs, isAddmoreConceptObs);
        return sectionPositionValues;
    }

    private void updateSectionPositionValues(List<SectionPositionValue> sectionPositionValues, Object objectValue, String sectionIndex, boolean isMultiselectObs, boolean isAddmoreObs) {
        if(objectValue instanceof List) {
            List<Object> values = (ArrayList) objectValue;
            for(int i = 0; i < values.size(); i++) {
                if(values.get(i) instanceof String) {
                    String value = (String) values.get(i);
                    if(isMultiselectObs) {
                        List<String> multiSelectObs = csvObservationHelper.getMultiSelectObsForJsonValue(value);
                        addObstoList(sectionPositionValues, sectionIndex, isMultiselectObs, isAddmoreObs, i, value, multiSelectObs);
                    } else if(isAddmoreObs) {
                        List<String> addmoreObs = csvObservationHelper.getAddmoreObsForJsonValue(value);
                        addObstoList(sectionPositionValues, sectionIndex, isMultiselectObs, isAddmoreObs, i, value, addmoreObs);
                    } else {
                        SectionPositionValue sectionPositionValue = new SectionPositionValue(value, sectionIndex, i, NOT_MULTISELECT_OBS_INDEX, NOT_ADDMORE_OBS_INDEX);
                        sectionPositionValues.add(sectionPositionValue);
                    }
                } else {
                    updateSectionPositionValues(sectionPositionValues, values.get(i), sectionIndex + SECTION_SPLITTER + i, isMultiselectObs, isAddmoreObs);
                }
            }
        }
    }

    private void addObstoList(List<SectionPositionValue> sectionPositionValues, String sectionIndex, boolean isMultiselectObs, boolean isAddmoreObs, int obsIndex, String value, List<String> multipleObs) {
        for(int i = 0; i < multipleObs.size(); i++) {
            SectionPositionValue sectionPositionValue = null;
            if(isMultiselectObs)
                sectionPositionValue = new SectionPositionValue(multipleObs.get(i), sectionIndex, obsIndex, i, NOT_ADDMORE_OBS_INDEX);
            else if(isMultiselectObs)
                sectionPositionValue = new SectionPositionValue(multipleObs.get(i), sectionIndex, obsIndex, NOT_MULTISELECT_OBS_INDEX, i);
            sectionPositionValues.add(sectionPositionValue);
        }
    }
}
