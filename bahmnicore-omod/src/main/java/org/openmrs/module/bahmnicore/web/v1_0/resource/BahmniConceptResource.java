package org.openmrs.module.bahmnicore.web.v1_0.resource;

import org.openmrs.Concept;
import org.openmrs.ConceptDescription;
import org.openmrs.ConceptName;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.PropertyGetter;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.representation.NamedRepresentation;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.DelegatingResourceDescription;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs2_0.ConceptResource2_0;
import org.openmrs.util.LocaleUtility;
import org.openmrs.util.OpenmrsConstants;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

@Resource(name = RestConstants.VERSION_1 + "/concept", supportedClass = Concept.class, supportedOpenmrsVersions = {"1.9.* - 2.*"}, order = 0)
public class BahmniConceptResource extends ConceptResource2_0 {

    public BahmniConceptResource() {
        allowedMissingProperties.add("hiNormal");
        allowedMissingProperties.add("hiAbsolute");
        allowedMissingProperties.add("hiCritical");
        allowedMissingProperties.add("lowNormal");
        allowedMissingProperties.add("lowAbsolute");
        allowedMissingProperties.add("lowCritical");
        allowedMissingProperties.add("units");
        allowedMissingProperties.add("allowDecimal");
        allowedMissingProperties.add("handler");
    }

    @Override
    protected PageableResult doSearch(RequestContext context) {
        return super.doSearch(context);
    }

    @Override
    public Concept getByUniqueId(String uuidOrName) {
        Concept byUniqueId = super.getByUniqueId(uuidOrName);
        if (byUniqueId != null) {
            return byUniqueId;
        }

        return Context.getConceptService().getConceptByName(uuidOrName);
    }

    @Override
    public DelegatingResourceDescription getRepresentationDescription(Representation rep) {

        DelegatingResourceDescription representationDescription = super.getRepresentationDescription(rep);
        if (representationDescription == null) {
            if (rep instanceof NamedRepresentation) {
                if (rep.getRepresentation().equals("bahmni")) {
                    DelegatingResourceDescription description = new DelegatingResourceDescription();
                    description.addProperty("uuid");
                    description.addProperty("name",Representation.DEFAULT);
                    description.addProperty("names",Representation.DEFAULT);
                    description.addProperty("set");
                    description.addProperty("datatype",Representation.DEFAULT);
                    description.addProperty("conceptClass",Representation.DEFAULT);
                    description.addProperty("hiNormal");
                    description.addProperty("hiAbsolute");
                    description.addProperty("hiCritical");
                    description.addProperty("lowNormal");
                    description.addProperty("lowAbsolute");
                    description.addProperty("lowCritical");
                    description.addProperty("units");
                    description.addProperty("allowDecimal");
                    description.addProperty("handler");
                    description.addProperty("descriptions", Representation.DEFAULT);
                    description.addProperty("answers", new NamedRepresentation("bahmniAnswer"));
                    description.addProperty("setMembers", new NamedRepresentation("bahmni"));
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
                    description.addProperty("set");
                    description.addProperty("version");
                    description.addProperty("retired");

                    description.addProperty("names", Representation.DEFAULT);
                    description.addProperty("descriptions", Representation.DEFAULT);

                    description.addProperty("mappings", Representation.DEFAULT);

                    description.addProperty("answers", new NamedRepresentation("bahmniAnswer"));
                    description.addProperty("setMembers", Representation.DEFAULT);
                    description.addProperty("auditInfo");
                    description.addSelfLink();
                    return description;
                }
            }
        }
        return representationDescription;
    }


    @PropertyGetter("names")
    public static Object getNames(Concept concept) {
        Locale globalDefaultLocale = LocaleUtility.getDefaultLocale();
        Locale userDefaultLocale = LocaleUtility.fromSpecification(Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE));

        Collection<ConceptName> names = concept.getNames(userDefaultLocale);
        if (globalDefaultLocale != userDefaultLocale) {
            names.addAll(concept.getNames(globalDefaultLocale));
        }
        return names;
    }

    @PropertyGetter("descriptions")
    public static Object getDescriptions(Concept concept) {
        Locale userDefaultLocale = LocaleUtility.fromSpecification(Context.getAuthenticatedUser().getUserProperty(OpenmrsConstants.USER_PROPERTY_DEFAULT_LOCALE));
        Collection<ConceptDescription> conceptDescriptions = new ArrayList<>();
        ConceptDescription conceptDescription = concept.getDescription(userDefaultLocale, false);
        if(conceptDescription != null){
            conceptDescriptions.add(conceptDescription);
        }
        return conceptDescriptions;
    }

}
