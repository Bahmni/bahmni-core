package org.bahmni.module.elisatomfeedclient;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bahmni.module.bahmnicore.properties.BahmniCoreProperties;
import org.openmrs.module.BaseModuleActivator;

public class Activator extends BaseModuleActivator {

    private Log log = LogFactory.getLog(this.getClass());
	
	@Override
	public void started() {
		log.info("Started the Open-Elis Atom Feed Client module");
		BahmniCoreProperties.load();
	}
	
	@Override
	public void stopped() {
		log.info("Stopped the Open-Elis Atom Feed Client module");
	}
}
