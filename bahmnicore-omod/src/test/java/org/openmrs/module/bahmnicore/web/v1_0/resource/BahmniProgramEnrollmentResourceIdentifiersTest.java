package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientProgram;
import org.openmrs.module.webservices.rest.web.representation.DefaultRepresentation;
import org.openmrs.module.webservices.rest.web.representation.FullRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.powermock.core.classloader.annotations.SuppressStaticInitializationFor;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(PowerMockRunner.class)
@SuppressStaticInitializationFor("org.openmrs.module.webservices.rest.web.RestConstants")
public class BahmniProgramEnrollmentResourceIdentifiersTest {

    @Test
    public void shouldReturnIdentifiersLinkedToProgram() {
        PatientProgram program = programWithPatient();
        PatientIdentifier linkedIdentifier = identifierLinkedTo(program);
        program.getPatient().addIdentifier(linkedIdentifier);

        List<PatientIdentifier> result = BahmniProgramEnrollmentResource.getIdentifiers(program);

        assertEquals(1, result.size());
        assertEquals(linkedIdentifier, result.get(0));
    }

    @Test
    public void shouldExcludeIdentifiersFromDifferentProgram() {
        PatientProgram program = programWithPatient();
        PatientProgram otherProgram = programWithPatient();
        PatientIdentifier otherIdentifier = identifierLinkedTo(otherProgram);
        program.getPatient().addIdentifier(otherIdentifier);

        List<PatientIdentifier> result = BahmniProgramEnrollmentResource.getIdentifiers(program);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldExcludeIdentifiersWithNoProgram() {
        PatientProgram program = programWithPatient();
        PatientIdentifier unlinkedIdentifier = new PatientIdentifier();
        program.getPatient().addIdentifier(unlinkedIdentifier);

        List<PatientIdentifier> result = BahmniProgramEnrollmentResource.getIdentifiers(program);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListWhenPatientHasNoIdentifiers() {
        PatientProgram program = programWithPatient();

        List<PatientIdentifier> result = BahmniProgramEnrollmentResource.getIdentifiers(program);

        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnFullRepresentationsForPatientAndIdentifiers() {
        DelegatingResourceDescription desc = new BahmniProgramEnrollmentResource().getRepresentationDescription(new FullRepresentation());
        assertEquals(Representation.FULL, desc.getProperties().get("patient").getRep());
        assertEquals(Representation.FULL, desc.getProperties().get("identifiers").getRep());
    }

    @Test
    public void shouldSetIdentifiersToRefRepWhenDefaultRepresentation() {
        DelegatingResourceDescription desc = new BahmniProgramEnrollmentResource().getRepresentationDescription(new DefaultRepresentation());
        assertEquals(Representation.REF, desc.getProperties().get("identifiers").getRep());
    }

    private PatientProgram programWithPatient() {
        PatientProgram program = new PatientProgram();
        program.setPatient(new Patient());
        return program;
    }

    private PatientIdentifier identifierLinkedTo(PatientProgram program) {
        PatientIdentifier identifier = new PatientIdentifier();
        identifier.setPatientProgram(program);
        return identifier;
    }
}
