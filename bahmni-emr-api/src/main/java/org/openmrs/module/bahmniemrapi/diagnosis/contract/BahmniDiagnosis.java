package org.openmrs.module.bahmniemrapi.diagnosis.contract;

import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BahmniDiagnosis extends EncounterTransaction.Diagnosis {
    private EncounterTransaction.Concept diagnosisStatusConcept;
    private BahmniDiagnosis firstDiagnosis;
    private BahmniDiagnosis latestDiagnosis;
    private boolean revised;

    public EncounterTransaction.Concept getDiagnosisStatusConcept() {
        return diagnosisStatusConcept;
    }

    public void setDiagnosisStatusConcept(EncounterTransaction.Concept diagnosisStatusConcept) {
        this.diagnosisStatusConcept = diagnosisStatusConcept;
    }

    public BahmniDiagnosis getFirstDiagnosis() {
        return firstDiagnosis;
    }

    public void setFirstDiagnosis(BahmniDiagnosis firstDiagnosis) {
        this.firstDiagnosis = firstDiagnosis;
    }

    public boolean isRevised() {
        return revised;
    }

    public void setRevised(boolean revised) {
        this.revised = revised;
    }

    public boolean isSame(EncounterTransaction.Diagnosis diagnosis) {
        if (getFreeTextAnswer() != null || diagnosis.getFreeTextAnswer() != null) {
            return isSameFreeTextAnswer(diagnosis);
        }
        return isSameCodedAnswer(diagnosis);
    }

    public boolean isDiagnosisWithSameExistingObs(EncounterTransaction.Diagnosis diagnosis) {
        if (StringUtils.isEmpty(getExistingObs())) {
            return false;
        }
        return getExistingObs().equals(diagnosis.getExistingObs()) ;
    }

    public boolean isSameFreeTextAnswer(EncounterTransaction.Diagnosis diagnosis) {
        if (getFreeTextAnswer() == null || diagnosis.getFreeTextAnswer() == null)
            return false;

        return getFreeTextAnswer().equals(diagnosis.getFreeTextAnswer());
    }

    public boolean isSameCodedAnswer(EncounterTransaction.Diagnosis diagnosis) {
        if (getCodedAnswer() == null || diagnosis.getCodedAnswer() == null)
            return false;

        return getCodedAnswer().getUuid().equals(diagnosis.getCodedAnswer().getUuid());
    }

    public BahmniDiagnosis getLatestDiagnosis() {
        return latestDiagnosis;
    }

    public void setLatestDiagnosis(BahmniDiagnosis latestDiagnosis) {
        this.latestDiagnosis = latestDiagnosis;
    }
}
