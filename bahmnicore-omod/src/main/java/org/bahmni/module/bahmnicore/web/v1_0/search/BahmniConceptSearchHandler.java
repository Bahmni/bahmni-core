package org.bahmni.module.bahmnicore.web.v1_0.search;

import org.apache.commons.collections.CollectionUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.api.SearchConfig;
import org.openmrs.module.webservices.rest.web.resource.api.SearchHandler;
import org.openmrs.module.webservices.rest.web.resource.api.SearchQuery;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.util.LocaleUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Component
public class BahmniConceptSearchHandler implements SearchHandler {

    @Autowired
    @Qualifier("conceptService")
    ConceptService conceptService;

    @Override
    public SearchConfig getSearchConfig() {
        SearchQuery searchQuery = new SearchQuery.Builder("Allows you to search for concepts by fully specified name").withRequiredParameters("name").build();
        return new SearchConfig("byFullySpecifiedName", RestConstants.VERSION_1 + "/concept", Arrays.asList("1.8.* - 2.*"), searchQuery);
    }

    @Override
    public PageableResult search(RequestContext context) throws ResponseException {
        String conceptName = context.getParameter("name");
        List<Locale> localeList = getLocales(context);

        List<ConceptSearchResult> conceptsSearchResult = conceptService.getConcepts(conceptName, localeList, false, null, null, null, null, null, 0, null);
        List<Concept> conceptsByName = conceptsSearchResult.stream().map(conceptSearchResult -> conceptSearchResult.getConcept()).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(conceptsByName)) {
            return new EmptySearchResult();
        } else {
            List<Concept> concepts = new ArrayList<Concept>();
            boolean isPreferredOrFullySpecified = false;
            for (Concept concept : conceptsByName) {
                for (ConceptName conceptname : concept.getNames()) {
                    if (conceptname.getName().equalsIgnoreCase(conceptName) && (conceptname.isPreferred() || conceptname.isFullySpecifiedName())) {
                        concepts.add(concept);
                        isPreferredOrFullySpecified = true;
                        break;
                    }
                }
            }
            if (!isPreferredOrFullySpecified)
                throw new APIException("The concept name should be either a fully specified or locale preferred name");
            return new NeedsPaging<Concept>(concepts, context);
        }
    }

    private List<Locale> getLocales(RequestContext context) {
        String locale = context.getParameter("locale");

        List<Locale> localeList = new ArrayList<>();
        localeList.add(LocaleUtility.getDefaultLocale());

        if (locale != null) {
            localeList.add(LocaleUtility.fromSpecification(locale));
        } else {
            localeList.add(Context.getLocale());
        }

        localeList = localeList.stream().distinct().collect(Collectors.toList());
        return localeList;
    }

}
