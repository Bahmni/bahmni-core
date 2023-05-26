# OpenMRS module bahmnicore

This module provides necessary services for running Bahmni

This module also contains global property **bahmni.lookupExternalTerminologyServer** to enable/ disable concept search through [FHIR Terminology Service Module](https://github.com/Bahmni/openmrs-module-snomed).
More details can be found [here](https://bahmni.atlassian.net/wiki/spaces/BAH/pages/3132686337/SNOMED+FHIR+Terminology+Server+Integration+with+Bahmni)

## Build

[![BahmniCore-master Actions Status](https://github.com/Bahmni/bahmni-core/workflows/Java%20CI%20with%20Maven/badge.svg)](https://github.com/Bahmni/bahmni-core/actions)

### Prerequisite
    JDK 1.8
    ruby 2.2+
    RubyGems
    Compass 1.0.3 (gem install compass)
    
### Clone the repository and build the omod
   
    git clone https://github.com/bahmni/bahmni-core
    cd bahmni-core
    ./mvnw clean install
    
## Deploy

Copy ```bahmni-core/bahmnicore-omod/target/bahmnicore-omod-VERSION-SNAPSHOT.omod``` into OpenMRS modules directory and restart OpenMRS
