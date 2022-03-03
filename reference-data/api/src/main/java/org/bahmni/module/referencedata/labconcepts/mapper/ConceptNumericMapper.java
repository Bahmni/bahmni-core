package org.bahmni.module.referencedata.labconcepts.mapper;

import org.apache.commons.lang3.StringUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptDatatype;
import org.openmrs.ConceptNumeric;

public class ConceptNumericMapper {

    public Concept map(Concept concept, org.bahmni.module.referencedata.labconcepts.contract.Concept conceptData, Concept existingConcept) {
        ConceptNumeric conceptNumeric = null;
        if (existingConcept == null || !existingConcept.getDatatype().getUuid().equals(ConceptDatatype.NUMERIC_UUID)) {
            conceptNumeric = new ConceptNumeric(concept);
        } else if (existingConcept.getDatatype().getUuid().equals(ConceptDatatype.NUMERIC_UUID)) {
            conceptNumeric = (ConceptNumeric) (concept);
        }
        if (conceptNumeric != null) {
            conceptNumeric.setUnits(conceptData.getUnits());
            setHiNormal(conceptData, conceptNumeric);
            setLowNormal(conceptData, conceptNumeric);
            setAllowDecimal(conceptData, conceptNumeric);
        }
        return conceptNumeric;
    }

    private void setLowNormal(org.bahmni.module.referencedata.labconcepts.contract.Concept conceptData, ConceptNumeric conceptNumeric) {
        String lowNormal = conceptData.getLowNormal();
        if (!StringUtils.isBlank(lowNormal)) {
            conceptNumeric.setLowNormal(Double.valueOf(lowNormal));
        }
    }

    private void setHiNormal(org.bahmni.module.referencedata.labconcepts.contract.Concept conceptData, ConceptNumeric conceptNumeric) {
        String hiNormal = conceptData.getHiNormal();
        if (!StringUtils.isBlank(hiNormal)) {
            conceptNumeric.setHiNormal(Double.valueOf(hiNormal));
        }
    }
    private void setAllowDecimal(org.bahmni.module.referencedata.labconcepts.contract.Concept conceptData, ConceptNumeric conceptNumeric) {
        String allowDecimal = conceptData.getAllowDecimal();
        if (!StringUtils.isBlank(allowDecimal)) {
            conceptNumeric.setAllowDecimal(Boolean.valueOf(allowDecimal));
        }
    }
}
