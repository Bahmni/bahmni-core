package org.bahmni.test.properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.util.OpenmrsUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class PropertiesReaderImpl implements PropertiesReader {
    private Properties properties;
    private static Log log = LogFactory.getLog(PropertiesReaderImpl.class);

    private PropertiesReaderImpl(Properties properties) {
        this.properties = properties;
    }

    public static PropertiesReaderImpl load() {
        String propertyFile = new File(OpenmrsUtil.getApplicationDataDirectory(), "bahmnicore.properties").getAbsolutePath();
        log.info(String.format("Reading bahmni properties from : %s", propertyFile));
        Properties properties;
        try {
            properties = new Properties(System.getProperties());
                properties.load(new FileInputStream(propertyFile));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new PropertiesReaderImpl(properties);
    }

    @Override
    public String getProperty(String key){
        return properties.getProperty(key);
    }
}
