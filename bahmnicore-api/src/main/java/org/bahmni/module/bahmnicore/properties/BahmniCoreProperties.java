package org.bahmni.module.bahmnicore.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class BahmniCoreProperties {
    private static Properties properties;
    private static Log log = LogFactory.getLog(BahmniCoreProperties.class);

    public static void load() {
        String propertyFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "bahmnicore.properties").getAbsolutePath();
        log.info(String.format("Reading bahmni properties from : %s", propertyFile));
        try {
            properties = new Properties(System.getProperties());
            properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String getProperty(String key){
        if(properties == null){
            load();
        }
        return properties.getProperty(key);
    }

    public static void initalize(Properties props) {
        properties = props;
    }
}
