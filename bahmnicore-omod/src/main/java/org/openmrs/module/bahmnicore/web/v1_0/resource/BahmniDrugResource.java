package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_12.DrugResource1_12;

@org.openmrs.module.webservices.rest.web.annotation.Resource(name = "v1/drug", supportedClass = org.openmrs.Drug
        .class, supportedOpenmrsVersions = {"1.10.* - 2.*"}, order = 0)
public class BahmniDrugResource extends DrugResource1_12 {


    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription representationDescription = super.getRepresentationDescription(rep);
        if (representationDescription == null && rep instanceof NamedRepresentation && rep.getRepresentation().equals("bahmniAnswer")) {
            DelegatingResourceDescription description = new DelegatingResourceDescription();
            description.addProperty("uuid");
            description.addProperty("name", Representation.DEFAULT);
            description.addProperty("displayString", findMethod("getDisplayString"));
            return description;
        }
        return representationDescription;
    }
}
