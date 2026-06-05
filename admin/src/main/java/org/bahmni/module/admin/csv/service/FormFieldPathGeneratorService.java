package org.bahmni.module.admin.csv.service;

import org.bahmni.csv.KeyValue;
import org.bahmni.form2.service.FormFieldPathService;
import org.bahmni.module.admin.csv.models.SectionPositionValue;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.bahmni.module.admin.observation.CSVObservationHelper.getLastItem;
import static org.springframework.util.CollectionUtils.isEmpty;

@Component
public class FormFieldPathGeneratorService {

    private static final String FORM_NAMESPACE = "Bahmni";
    private static final String FORM_FIELD_PATH_SEPARATOR = "/";
    private Map<String, Boolean> formFieldPathAddmoreAttribute = new HashMap<>();
    private Map<String, List<Integer>> addmoreSectionIndices = new HashMap<>();

    private FormFieldPathService formFieldPathService;

    @Autowired
    public FormFieldPathGeneratorService(FormFieldPathService formFieldPathService) {
        this.formFieldPathService = formFieldPathService;
    }

    public void setFormNamespaceAndFieldPath(List<EncounterTransaction.Observation> form2Observations, List<String> form2CSVHeaderParts) {
        if (isEmpty(form2Observations)) {
            return;
        }
        final EncounterTransaction.Observation observation = getLastItem(form2Observations);
        final String formFieldPath = formFieldPathService.getFormFieldPath(form2CSVHeaderParts);
        observation.setFormFieldPath(formFieldPath);
        observation.setFormNamespace(FORM_NAMESPACE);
    }

    public void setFormNamespaceAndFieldPathForMultiSelectObs(List<EncounterTransaction.Observation> form2Observations, List<String> form2CSVHeaderParts, List<KeyValue> multiSelectForm2CSVObservations) {
        if (isEmpty(form2Observations)) {
            return;
        }
        int prevObsCount = form2Observations.size() - multiSelectForm2CSVObservations.size();
        for(int i = 0; i < multiSelectForm2CSVObservations.size(); i++) {
            final EncounterTransaction.Observation observation = form2Observations.get(prevObsCount + i);
            final String formFieldPath = formFieldPathService.getFormFieldPath(form2CSVHeaderParts);
            observation.setFormFieldPath(formFieldPath);
            observation.setFormNamespace(FORM_NAMESPACE);
        }
    }

    public void setFormNamespaceAndFieldPathForAddmoreObs(List<EncounterTransaction.Observation> form2Observations, List<String> form2CSVHeaderParts, List<KeyValue> addmoreForm2CSVObservations) {
        if (isEmpty(form2Observations)) {
            return;
        }
        int prevObsCount = form2Observations.size() - addmoreForm2CSVObservations.size();
        final String formFieldPath = formFieldPathService.getFormFieldPath(form2CSVHeaderParts);
        String[] tokens = formFieldPath.split(FORM_FIELD_PATH_SEPARATOR);
        int formFieldPathPosition = tokens.length - 1;
        String path = tokens[formFieldPathPosition];
        String controlIdPrefix = path.split("-")[0];

        for(int i = 0; i < addmoreForm2CSVObservations.size(); i++) {
            final EncounterTransaction.Observation observation = form2Observations.get(prevObsCount + i);
            tokens[formFieldPathPosition] = controlIdPrefix + "-" + i;
            observation.setFormFieldPath(String.join(FORM_FIELD_PATH_SEPARATOR, tokens));
            observation.setFormNamespace(FORM_NAMESPACE);
        }
    }

    public void setFormNamespaceAndFieldPathForJsonValue(List<EncounterTransaction.Observation> form2Observations, List<String> form2CSVHeaderParts, List<KeyValue> addmoreSectionCSVObservations, List<SectionPositionValue> sectionPositionValues) {
        if (isEmpty(form2Observations)) {
            return;
        }

        updateFormFieldPathWithAddmoreAttribute(form2CSVHeaderParts);

        int prevObsCount = form2Observations.size() - addmoreSectionCSVObservations.size();
        final String formFieldPath = formFieldPathService.getFormFieldPath(form2CSVHeaderParts);

        for(int i = 0; i < addmoreSectionCSVObservations.size(); i++) {
            final EncounterTransaction.Observation observation = form2Observations.get(prevObsCount + i);
            updateObsWithFormFieldPath(observation, form2CSVHeaderParts, sectionPositionValues, prevObsCount, formFieldPath, i);
        }
    }

    private void updateObsWithFormFieldPath(EncounterTransaction.Observation observation, List<String> form2CSVHeaderParts, List<SectionPositionValue> sectionPositionValues, int prevObsCount, String formFieldPath, int csvObservationIndex) {
        String[] tokens = formFieldPath.split(FORM_FIELD_PATH_SEPARATOR);
        List<Integer> indicesInJson = addmoreSectionIndices.get(form2CSVHeaderParts.toString());
        String sectionPositionIndex = sectionPositionValues.get(csvObservationIndex).getSectionIndex();

        // update form field path for sections based on JSON value
        for(int j = 0; j < indicesInJson.size() - 1; j++) {
            int addmoreSectionIndex = 0;
            int sectionIndexPosition = indicesInJson.get(j);
            String partialFormFieldPath = tokens[sectionIndexPosition];
            String controlIdPrefix = partialFormFieldPath.split("-")[0];
            if(sectionPositionIndex.contains(FORM_FIELD_PATH_SEPARATOR)) {
                String[] indices = sectionPositionIndex.split(FORM_FIELD_PATH_SEPARATOR);
                addmoreSectionIndex = Integer.parseInt(indices[j+1]);
            } else {
                addmoreSectionIndex = Integer.parseInt(sectionPositionIndex);
            }
            tokens[sectionIndexPosition] = controlIdPrefix + "-" + addmoreSectionIndex;
        }

        // update form field path for section having observation.
        int sectionWithObsIndexPosition = indicesInJson.get(indicesInJson.size() - 1);
        String partialFormFieldPath = tokens[sectionWithObsIndexPosition];
        String controlIdPrefix = partialFormFieldPath.split("-")[0];
        tokens[sectionWithObsIndexPosition] = controlIdPrefix + "-" + sectionPositionValues.get(csvObservationIndex).getValueIndex();

        if(sectionPositionValues.get(csvObservationIndex).getAddmoreIndex() != -1) {
            int obsAddmoreIndex = sectionPositionValues.get(csvObservationIndex).getAddmoreIndex();
            String addmoreFormControlId = tokens[tokens.length - 1];
            String addmoreControlIdPrefix = addmoreFormControlId.split("-")[0];
            tokens[tokens.length - 1] = addmoreControlIdPrefix + "-" + obsAddmoreIndex;
        }
        observation.setFormFieldPath(String.join(FORM_FIELD_PATH_SEPARATOR, tokens));
        observation.setFormNamespace(FORM_NAMESPACE);
    }

    private void updateFormFieldPathWithAddmoreAttribute(List<String> form2CSVHeaderParts) {
        if(formFieldPathAddmoreAttribute.get(form2CSVHeaderParts.toString()) == null) {
           List<Integer> indices = new ArrayList<>();
           boolean isFirstAddmoreIdentified = false;
           int intialSectionsWithoutAddmore = 0;
           for (int i = 1; i < form2CSVHeaderParts.size(); i++) {
               List<String> headerPartsList = form2CSVHeaderParts.subList(0, i + 1);
               boolean addmore = formFieldPathService.isAddmore(headerPartsList);
               if(!addmore && !isFirstAddmoreIdentified) {
                   isFirstAddmoreIdentified = true;
                   intialSectionsWithoutAddmore++;
               }
               formFieldPathAddmoreAttribute.put(headerPartsList.toString(), addmore);
               if(addmore)
                   indices.add(i - intialSectionsWithoutAddmore);
           }
           addmoreSectionIndices.put(form2CSVHeaderParts.toString(), indices);
        }
    }
}
