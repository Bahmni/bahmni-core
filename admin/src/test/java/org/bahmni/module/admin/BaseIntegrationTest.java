package org.bahmni.module.admin;

import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(locations = {"classpath:TestingApplicationContext.xml"}, inheritLocations = true)
public class BaseIntegrationTest extends BaseModuleWebContextSensitiveTest {
}
