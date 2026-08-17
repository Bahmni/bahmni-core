package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.ConceptNumeric;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_5.ConceptReferenceRangeResource2_5;

/**
 * Override for ConceptReferenceRange in OpenMRS version 2.5.*-2.6.*
 * This is a temporary fix till 2.7.*
 *
 * Concept Reference Ranges are introduced in OpenMRS 2.7.0,
 * However, the REST API is not updated yet. With WebServices rest version 2.50,
 * Resource representation for ConceptReferenceRange breaks when representation is 'bahmni' (v=bahmni)
 * TODO: Remove/Refactor this class once moved to to 2.7.*.
 *
 * {@link Resource} override for ConceptReferenceRange.
 */
@Resource(
        name = RestConstants.VERSION_1 + "/conceptreferencerange",
        supportedClass = ConceptNumeric.class,
        supportedOpenmrsVersions = {"2.5.* - 2.6.*"},
        order = 0
)
public class BahmniConceptReferenceRangeResource2_5 extends ConceptReferenceRangeResource2_5 {
    public BahmniConceptReferenceRangeResource2_5() {
        allowedMissingProperties.add("hiNormal");
        allowedMissingProperties.add("hiAbsolute");
        allowedMissingProperties.add("hiCritical");
        allowedMissingProperties.add("lowNormal");
        allowedMissingProperties.add("lowAbsolute");
        allowedMissingProperties.add("lowCritical");
        allowedMissingProperties.add("units");
        allowedMissingProperties.add("allowDecimal");
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription representationDescription = super.getRepresentationDescription(rep);
        if (representationDescription != null) {
            return representationDescription;
        }

        if (rep instanceof NamedRepresentation && "bahmni".equals(rep.getRepresentation())) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();


            description.addProperty("uuid");
            description.addProperty("display");

            description.addProperty("name", Representation.DEFAULT);
            description.addProperty("names", Representation.DEFAULT);
            description.addProperty("datatype", Representation.DEFAULT);
            description.addProperty("conceptClass", Representation.DEFAULT);
            description.addProperty("descriptions", Representation.DEFAULT);

            description.addProperty("hiNormal");
            description.addProperty("hiAbsolute");
            description.addProperty("hiCritical");
            description.addProperty("lowNormal");
            description.addProperty("lowAbsolute");
            description.addProperty("lowCritical");
            description.addProperty("units");
            description.addProperty("allowDecimal");
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        } else if (rep.getRepresentation().equals("bahmniAnswer")) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid", Representation.DEFAULT);
            description.addProperty("name", Representation.DEFAULT);
            description.addProperty("names", Representation.DEFAULT);
            description.addProperty("displayString");
            return description;
        } else if (rep.getRepresentation().equals("bahmniFullAnswers")) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("display");
            description.addProperty("name", Representation.DEFAULT);
            description.addProperty("datatype", Representation.DEFAULT);
            description.addProperty("conceptClass", Representation.DEFAULT);
            description.addProperty("version");
            description.addProperty("retired");
            description.addProperty("names", Representation.DEFAULT);
            description.addProperty("descriptions", Representation.DEFAULT);
            description.addProperty("mappings", Representation.DEFAULT);
            description.addProperty("auditInfo");
            description.addSelfLink();
            return description;
        }

        return null;
    }
}