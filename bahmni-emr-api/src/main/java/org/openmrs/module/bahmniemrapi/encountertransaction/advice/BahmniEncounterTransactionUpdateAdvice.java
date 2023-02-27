package org.openmrs.module.bahmniemrapi.encountertransaction.advice;

import groovy.lang.GroovyClassLoader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openmrs.module.bahmniemrapi.encountertransaction.contract.BahmniEncounterTransaction;
import org.openmrs.module.bahmniemrapi.obscalculator.ObsValueCalculator;
import org.openmrs.util.OpenmrsUtil;
import org.springframework.aop.MethodBeforeAdvice;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.Method;
import java.nio.file.Paths;
import java.sql.Timestamp;

public class BahmniEncounterTransactionUpdateAdvice implements MethodBeforeAdvice {

    private static Logger logger = LogManager.getLogger(BahmniEncounterTransactionUpdateAdvice.class);
    
    private static String BAHMNI_OBS_VALUE_CALCULATOR_FILENAME = "BahmniObsValueCalculator.groovy";
    
    @Override
    public void before(Method method, Object[] args, Object target) throws Throwable {
        System.out.println("---> Start before method: " + new Timestamp(new java.util.Date().getTime()));
        logger.info( "{}: Start", this.getClass().getName());
        GroovyClassLoader gcl = new GroovyClassLoader();
        System.out.println("---> Start Paths.get method: " + new Timestamp(new java.util.Date().getTime()));
        String fileName = Paths.get(
        		OpenmrsUtil.getApplicationDataDirectory(),
        		"obscalculator",
        		BAHMNI_OBS_VALUE_CALCULATOR_FILENAME
        		).toString();
        System.out.println("---> End Paths.get method: " + new Timestamp(new java.util.Date().getTime()));
        Class clazz;
        try {
            System.out.println("---> Inside try block " + new Timestamp(new java.util.Date().getTime()));
            clazz = gcl.parseClass(new File(fileName));
            System.out.println("---> try block completed" + new Timestamp(new java.util.Date().getTime()));
        } catch (FileNotFoundException fileNotFound) {
            logger.error("Could not find {} : {}. Possible system misconfiguration. {} ", ObsValueCalculator.class.getName(), fileName, fileNotFound);
            return;
        }
        logger.info(  "{} : Using rules in {}", this.getClass().getName(), clazz.getName());
        ObsValueCalculator obsValueCalculator = (ObsValueCalculator) clazz.newInstance();
        System.out.println("---> Obsvaluecalculator run started " + new Timestamp(new java.util.Date().getTime()));
        obsValueCalculator.run((BahmniEncounterTransaction) args[0]);
        System.out.println("---> Obsvaluecalculator run done " + new Timestamp(new java.util.Date().getTime()));
        logger.info( " {}: Done", this.getClass().getName());
        System.out.println("---> End before method " + new Timestamp(new java.util.Date().getTime()));
    }
    
}
