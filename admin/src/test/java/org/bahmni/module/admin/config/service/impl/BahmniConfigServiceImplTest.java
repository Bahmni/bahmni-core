package org.bahmni.module.admin.config.service.impl;

import org.bahmni.module.admin.config.dao.impl.BahmniConfigDaoImpl;
import org.bahmni.module.admin.config.model.BahmniConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.User;
import org.openmrs.api.context.Context;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class BahmniConfigServiceImplTest {
    @Mock
    private BahmniConfigDaoImpl bahmniConfigDao;

    private BahmniConfig existingConfig;
    private BahmniConfig newConfig;
    private BahmniConfigServiceImpl bahmniConfigService;
    private User creator;

    @Before
    public void setUp() throws Exception {
        initMocks(this);
        creator = new User();
        PowerMockito.mockStatic(Context.class);
        when(Context.getAuthenticatedUser()).thenReturn(creator);

        existingConfig = new BahmniConfig();
        existingConfig.setUuid("existing");
        newConfig = new BahmniConfig();
        when(bahmniConfigDao.get("existing")).thenReturn(existingConfig);
        when(bahmniConfigDao.get("new")).thenReturn(null);
        when(bahmniConfigDao.save(any(BahmniConfig.class))).then(new Answer<BahmniConfig>() {
            @Override
            public BahmniConfig answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                BahmniConfig argument = (BahmniConfig) arguments[0];
                argument.setUuid("new");
                return argument;
            }
        });
        when(bahmniConfigDao.update(any(BahmniConfig.class))).then(new Answer<BahmniConfig>() {
            @Override
            public BahmniConfig answer(InvocationOnMock invocationOnMock) throws Throwable {
                Object[] arguments = invocationOnMock.getArguments();
                return (BahmniConfig) arguments[0];
            }
        });
        bahmniConfigService = new BahmniConfigServiceImpl(bahmniConfigDao);
    }

    @Test
    public void updateChangedAuditFieldsAndConfigForExistingConfigs() throws Exception {
        existingConfig.setConfig("Modified Config");
        assertNull(existingConfig.getChangedBy());
        assertNull(existingConfig.getDateChanged());
        BahmniConfig savedConfig = bahmniConfigService.update(existingConfig);
        assertNotNull(savedConfig.getDateChanged());
        assertEquals(creator, savedConfig.getChangedBy());
        assertEquals("Modified Config", savedConfig.getConfig());
    }

    @Test
    public void createNewConfigWithCreator() throws Exception {
        newConfig.setConfig("Yo Config");
        assertNull(newConfig.getDateCreated());
        assertNull(newConfig.getCreator());
        BahmniConfig savedConfig = bahmniConfigService.save(newConfig);
        assertNotNull(savedConfig.getDateCreated());
        assertEquals(creator, savedConfig.getCreator());
        assertEquals("Yo Config", savedConfig.getConfig());
    }
}