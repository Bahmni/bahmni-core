package org.openmrs.module.bahmniemrapi.diagnosis.contract;

import org.junit.Test;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BahmniDiagnosisTest {
    @Test
    public void isSameReturnsTrueIfCodedAnswersAreSame() {
        EncounterTransaction.Concept malariaDiagnosis = new EncounterTransaction.Concept("uuid", "Malaria");

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(malariaDiagnosis);

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setCodedAnswer(malariaDiagnosis);

        assertTrue("both diagnosis for malaria and are same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsFalseIfCodedAnswersAreNotSame() {
        EncounterTransaction.Concept malariaDiagnosis = new EncounterTransaction.Concept("uuid1", "Malaria");
        EncounterTransaction.Concept tbDiagnosis = new EncounterTransaction.Concept("uuid2", "TB");

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(malariaDiagnosis);

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setCodedAnswer(tbDiagnosis);

        assertFalse("diagnoses are not same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsTrueIfFreeTextAnswersAreSame() {
        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setFreeTextAnswer("Malaria");

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setFreeTextAnswer("Malaria");

        assertTrue("both diagnosis for malaria and are same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsFalseIfFreeTextAnswersAreNotSame() {
        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setFreeTextAnswer("Malaria");

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setFreeTextAnswer("TB");

        assertFalse("diagnoses are not same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsFalseWhenCodedAnswerIsNull() {
        EncounterTransaction.Concept malariaDiagnosis = new EncounterTransaction.Concept("uuid", "Malaria");

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setCodedAnswer(malariaDiagnosis);

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setFreeTextAnswer("Malaria");

        assertFalse("diagnoses are not same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsFalseWhenFreeTextAnswerIsNull() {
        EncounterTransaction.Concept malariaDiagnosis = new EncounterTransaction.Concept("uuid", "Malaria");

        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        bahmniDiagnosis.setFreeTextAnswer("Malaria");

        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();
        diagnosis.setCodedAnswer(malariaDiagnosis);

        assertFalse("diagnoses are not same", bahmniDiagnosis.isSame(diagnosis));
    }

    @Test
    public void isSameReturnsFalseWhenBothAreNull() {
        BahmniDiagnosis bahmniDiagnosis = new BahmniDiagnosis();
        EncounterTransaction.Diagnosis diagnosis = new EncounterTransaction.Diagnosis();

        assertFalse("diagnoses are not same", bahmniDiagnosis.isSame(diagnosis));
    }
}
