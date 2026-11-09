package org.openmrs.module.bahmniemrapi.disposition.mapper;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.*;
import org.openmrs.api.ConceptService;
import org.openmrs.Person;
import org.openmrs.PersonName;
import org.openmrs.User;
import org.openmrs.module.bahmniemrapi.disposition.contract.BahmniDisposition;
import org.openmrs.module.emrapi.encounter.domain.EncounterTransaction;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.Locale;

import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

public class BahmniDispositionMapperTest {

    private BahmniDispositionMapper bahmniDispositionMapper;
    @Mock
    private ConceptService conceptService;
    @Mock
    private EncounterTransaction.Disposition mockDisposition;
    @Mock
    private Concept dispositionConcept;
    @Mock
    private ConceptName conceptName;

    @Before
    public void setUp(){
        initMocks(this);

        Locale locale = new Locale("en");
        String conceptNameString = "Absconding";
        when(conceptService.getConcept(conceptNameString)).thenReturn(dispositionConcept);
        when(dispositionConcept.getPreferredName(locale)).thenReturn(conceptName);
        when(conceptName.getName()).thenReturn(conceptNameString);
        bahmniDispositionMapper = new BahmniDispositionMapper(conceptService);
    }

    @Test
    public void ensureBahmniDispositionIsPopulated(){
        EncounterTransaction.Provider provider = new EncounterTransaction.Provider();
        provider.setName("Sample Provider");
        provider.setUuid("1234Uuid");

        Set<EncounterTransaction.Provider> providers = new HashSet();
        providers.add(provider);

        Date dispositionDate = new Date();

        EncounterTransaction.Disposition disposition= new EncounterTransaction.Disposition();
        disposition.setCode("1234")
                .setExistingObs("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d")
                .setVoided(false)
                .setVoidReason(null)
                .setDispositionDateTime(dispositionDate);

        disposition.setConceptName("Absconding");
        disposition.setAdditionalObs(new ArrayList<EncounterTransaction.Observation>());


        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName("testPersonName");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(personName);
        person.setNames(personNames);
        User user = new User(person);


        BahmniDisposition bahmniDisposition = bahmniDispositionMapper.map(disposition, providers, user , new Locale("en"));

        Assert.assertEquals("1234",bahmniDisposition.getCode());
        Assert.assertEquals("a26a8c32-6fc1-4f5e-8a96-f5f5b05b87d",bahmniDisposition.getExistingObs());
        Assert.assertFalse(bahmniDisposition.isVoided());
        Assert.assertNull(bahmniDisposition.getVoidReason());
        Assert.assertEquals(dispositionDate,bahmniDisposition.getDispositionDateTime());
        Assert.assertEquals("Absconding", bahmniDisposition.getConceptName());
        Assert.assertEquals(0, bahmniDisposition.getAdditionalObs().size());

        EncounterTransaction.Provider actualProvider = bahmniDisposition.getProviders().iterator().next();
        Assert.assertEquals("Sample Provider", actualProvider.getName());
        Assert.assertEquals("1234Uuid", actualProvider.getUuid());

    }

    @Test
    public void shouldNotThrowWhenConceptCannotBeResolved(){
        String unresolvableConceptName = "Admit Patient";
        when(conceptService.getConcept(unresolvableConceptName)).thenReturn(null);

        EncounterTransaction.Disposition disposition = new EncounterTransaction.Disposition();
        disposition.setConceptName(unresolvableConceptName);
        disposition.setAdditionalObs(new ArrayList<EncounterTransaction.Observation>());

        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName("testPersonName");
        Set<PersonName> personNames = new HashSet<>();
        personNames.add(personName);
        person.setNames(personNames);
        User user = new User(person);

        BahmniDisposition bahmniDisposition = bahmniDispositionMapper.map(disposition, new HashSet<EncounterTransaction.Provider>(), user, new Locale("en"));

        Assert.assertEquals(unresolvableConceptName, bahmniDisposition.getConceptName());
        Assert.assertNull(bahmniDisposition.getPreferredName());
    }


    private Concept dispositionConcept(String code, String name, Locale locale) {
        Concept concept = new Concept();
        if (name != null) {
            ConceptName conceptName = new ConceptName(name, locale);
            conceptName.setLocalePreferred(true);
            concept.addName(conceptName);
        }
        if (code != null) {
            ConceptSource source = new ConceptSource();
            source.setName("org.openmrs.module.emrapi");
            ConceptReferenceTerm term = new ConceptReferenceTerm();
            term.setConceptSource(source);
            term.setCode(code);
            ConceptMap conceptMap = new ConceptMap();
            conceptMap.setConceptReferenceTerm(term);
            concept.setConceptMappings(new HashSet<>(java.util.Collections.singletonList(conceptMap)));
        }
        return concept;
    }

    private Order adtOrder(Concept concept) {
        Order order = new Order();
        order.setConcept(concept);
        order.setDateActivated(new Date());
        return order;
    }

    @Test
    public void shouldMapAdtOrderToBahmniDisposition() {
        Locale locale = new Locale("en");
        Order order = adtOrder(dispositionConcept("ADMIT", "Admit Patient", locale));
        Person person = new Person();
        PersonName personName = new PersonName();
        personName.setGivenName("testPersonName");
        person.addName(personName);
        order.setCreator(new User(person));
        Set<EncounterTransaction.Provider> providers = new HashSet<>();

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, providers, locale);

        Assert.assertEquals("ADMIT", result.getCode());
        Assert.assertEquals("Admit Patient", result.getConceptName());
        Assert.assertEquals("Admit Patient", result.getPreferredName());
        Assert.assertEquals(order.getEffectiveStartDate(), result.getDispositionDateTime());
        Assert.assertFalse(result.isVoided());
        Assert.assertNull(result.getExistingObs());
        Assert.assertSame(providers, result.getProviders());
        Assert.assertTrue(result.getCreatorName().contains("testPersonName"));
    }

    @Test
    public void shouldMapVoidedAdtOrderWithVoidReason() {
        Locale locale = new Locale("en");
        Order order = adtOrder(dispositionConcept("DISCHARGE", "Discharge Patient", locale));
        order.setVoided(true);
        order.setVoidReason("entered in error");

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), locale);

        Assert.assertTrue(result.isVoided());
        Assert.assertEquals("entered in error", result.getVoidReason());
    }

    @Test
    public void shouldLeaveCodeNullWhenConceptHasNoEmrapiMapping() {
        Locale locale = new Locale("en");
        Order order = adtOrder(dispositionConcept(null, "Admit Patient", locale));

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), locale);

        Assert.assertNull(result.getCode());
    }

    @Test
    public void shouldIgnoreMappingsFromOtherSources() {
        Locale locale = new Locale("en");
        Concept concept = dispositionConcept("ADMIT", "Admit Patient", locale);
        concept.getConceptMappings().iterator().next().getConceptReferenceTerm().getConceptSource().setName("other.source");

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(adtOrder(concept), new HashSet<EncounterTransaction.Provider>(), locale);

        Assert.assertNull(result.getCode());
    }

    @Test
    public void shouldLeaveCreatorAndPreferredNameNullWhenAbsent() {
        Order order = adtOrder(dispositionConcept("ADMIT", null, null));

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), new Locale("en"));

        Assert.assertNull(result.getCreatorName());
        Assert.assertNull(result.getPreferredName());
    }

    @Test
    public void shouldLeaveConceptNameNullWhenConceptHasNoNames() {
        Order order = adtOrder(dispositionConcept("ADMIT", null, null));

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), new Locale("en"));

        Assert.assertNull(result.getConceptName());
    }

    @Test
    public void shouldLeaveCreatorNameNullWhenCreatorHasNoPersonName() {
        Order order = adtOrder(dispositionConcept("ADMIT", "Admit Patient", new Locale("en")));
        order.setCreator(new User(new Person()));

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), new Locale("en"));

        Assert.assertNull(result.getCreatorName());
    }

    @Test
    public void shouldDefaultToEnglishWhenLocaleIsNull() {
        Order order = adtOrder(dispositionConcept("ADMIT", "Admit Patient", new Locale("en")));

        BahmniDisposition result = bahmniDispositionMapper.mapFromOrder(order, new HashSet<EncounterTransaction.Provider>(), null);

        Assert.assertEquals("Admit Patient", result.getConceptName());
        Assert.assertEquals("Admit Patient", result.getPreferredName());
    }

}
