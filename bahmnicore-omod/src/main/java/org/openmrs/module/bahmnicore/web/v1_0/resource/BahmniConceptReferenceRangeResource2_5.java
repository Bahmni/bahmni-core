package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.ConceptNumeric;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;

import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_5.ConceptReferenceRangeResource2_5;

@Resource(
        name = RestConstants.VERSION_1 + "/conceptreferencerange",
        supportedClass = ConceptNumeric.class,
        supportedOpenmrsVersions = {"2.5.* - 2.6.*"},
        order = 0
)
public class BahmniConceptReferenceRangeResource2_5 extends ConceptReferenceRangeResource2_5 {

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription parent = super.getRepresentationDescription(rep);
        if (parent != null) {
            return parent;
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


            description.addProperty("answers", Representation.DEFAULT);
            description.addProperty("setMembers", Representation.DEFAULT);
            description.addSelfLink();
            description.addLink("full", ".?v=" + RestConstants.REPRESENTATION_FULL);
            return description;
        }

        return null;
    }
}