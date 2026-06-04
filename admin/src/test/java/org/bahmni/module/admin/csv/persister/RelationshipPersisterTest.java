package org.bahmni.module.admin.csv.persister;

import org.bahmni.csv.Messages;
import org.bahmni.module.admin.csv.models.RelationshipRow;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@PowerMockIgnore("javax.management.*")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Context.class})
public class RelationshipPersisterTest {

    @Mock
    private AdministrationService administrationService;

    @Mock
    private UserContext userContext;

    @Rule
    public ExpectedException expectedEx = ExpectedException.none();

    @Before
    public void setUp() {
        initMocks(this);
        PowerMockito.mockStatic(Context.class);
        when(Context.getAdministrationService()).thenReturn(administrationService);
        when(administrationService.getGlobalProperty(eq("bahmni.admin.csv.upload.dateFormat"))).thenReturn("yyyy-M-d");
    }

    @Test
    public void shouldPassValidationIfAllRequiredFieldsAreProvided() throws Exception {
        Messages messages = getRelationshipPersister().validate(new RelationshipRow("GAN200012", "GAN200015", "Parent", "Child", "2014-02-01", "2015-01-01"));
        assertEquals(0, messages.size());
    }

    @Test
    public void shouldThrowExceptionIfPersonAIsNotSpecified() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Patient unique identifier not specified.");
        getRelationshipPersister().validateRow(new RelationshipRow(null, "GAN200015", "", "Child", "2014-02-01", "2015-01-01"));
    }

    @Test
    public void shouldThrowExceptionIfProviderNameAndRelationalIdentifierIsNotSpecified() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Both Provider Name and Relation Identifier cannot be null.");
        getRelationshipPersister().validateRow(new RelationshipRow("GAN200015", null, "", "Child", "2014-02-01", "2015-01-01"));
    }

    @Test
    public void shouldThrowExceptionIfRelationshipBIsToANotSpecified() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Relationship type is not specified.");
        getRelationshipPersister().validateRow(new RelationshipRow("GAN200015", "GAN200016", "ProviderName", null, "2014-02-01", "2015-01-01"));
    }


    @Test
    public void shouldThrowExceptionIfEndDateIsBeforeStartDate() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Start date should be before end date.");
        getRelationshipPersister().validateRow(new RelationshipRow("GAN200012", "GAN200015", "ProviderName", "Child", "2015-02-01", "2014-01-01"));
    }

    @Test
    public void shouldThrowExceptionIfTheStartDateFormatIsWrong() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Date format 02-01-2015 doesn't match `bahmni.admin.csv.upload.dateFormat` global property, expected format yyyy-M-d");
        getRelationshipPersister().validateRow(new RelationshipRow("GAN200012", "GAN200015", "ProviderName", "Child", "02-01-2015", "2014-01-01"));
    }

    @Test
    public void shouldThrowExceptionIfTheEndDateFormatIsWrong() throws Exception {
        expectedEx.expect(RuntimeException.class);
        expectedEx.expectMessage("Date format 01-01-2014 doesn't match `bahmni.admin.csv.upload.dateFormat` global property, expected format yyyy-M-d");
        getRelationshipPersister().validateRow(new RelationshipRow("GAN200012", "GAN200015", "ProviderName", "Child", "2015-02-01", "01-01-2014"));
    }

    private RelationshipPersister getRelationshipPersister() {
        return new RelationshipPersister();
    }


}