package org.bahmni.module.referencedata.addresshierarchy;

import org.ict4h.atomfeed.transaction.AFTransactionWorkWithoutResult;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.openmrs.api.context.Context;
import org.openmrs.api.context.UserContext;
import org.openmrs.module.addresshierarchy.AddressHierarchyEntry;
import org.openmrs.module.addresshierarchy.service.AddressHierarchyService;
import org.openmrs.module.atomfeed.transaction.support.AtomFeedSpringTransactionManager;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AddressHierarchyEntryEventInterceptorTest {
    private AtomFeedSpringTransactionManager atomFeedSpringTransactionManager;
    @Mock
    private UserContext userContext;

    private AddressHierarchyEntryEventInterceptor publishedFeed;
    private AddressHierarchyEntry addressHierarchyEntry;

    private MockedStatic<Context> contextMockedStatic;
    private MockedConstruction<AtomFeedSpringTransactionManager> atomFeedSpringTransactionManagerMockedConstruction;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        addressHierarchyEntry = new AddressHierarchyEntry();
        addressHierarchyEntry.setUuid("uuid");
        addressHierarchyEntry.setUserGeneratedId("707070");
        contextMockedStatic = Mockito.mockStatic(Context.class);

        ArrayList<PlatformTransactionManager> platformTransactionManagers = new ArrayList<>();
        platformTransactionManagers.add(new HibernateTransactionManager());
        contextMockedStatic.when(() -> Context.getRegisteredComponents(PlatformTransactionManager.class)).thenReturn(platformTransactionManagers);
        atomFeedSpringTransactionManagerMockedConstruction = Mockito.mockConstruction(AtomFeedSpringTransactionManager.class,
                (mock, context) -> atomFeedSpringTransactionManager = mock);
        publishedFeed = new AddressHierarchyEntryEventInterceptor();

    }

    @After
    public void tearDown() {
        atomFeedSpringTransactionManagerMockedConstruction.close();
        contextMockedStatic.close();
    }

    @Test
    public void shouldPublishToFeedAfterSavingAddressHierarchyEntry() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntry", AddressHierarchyEntry.class);
        Object[] objects = new Object[]{addressHierarchyEntry};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldPublishToFeedAfterSavingAddressHierarchyEntries() throws Throwable {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(addressHierarchyEntry);
        entries.add(addressHierarchyEntry);
        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);
        verify(atomFeedSpringTransactionManager, times(2)).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldNotCreateEventIfParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);

        publishedFeed.afterReturning(null, method, null, null);

        verify(atomFeedSpringTransactionManager, never()).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }

    @Test
    public void shouldNotCreateEventIfEntryInParameterIsNull() throws Exception {
        Method method = AddressHierarchyService.class.getMethod("saveAddressHierarchyEntries", List.class);
        ArrayList<Object> entries = new ArrayList<>();
        entries.add(null);

        Object[] objects = new Object[]{entries};

        publishedFeed.afterReturning(null, method, objects, null);

        verify(atomFeedSpringTransactionManager, never()).executeWithTransaction(any(AFTransactionWorkWithoutResult.class));
    }
}
