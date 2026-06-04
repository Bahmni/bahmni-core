#!/bin/sh -x

USER=bahmni

rm -f /opt/openmrs/modules/*
ln -s /bahmni-code/openmrs-distro-bahmni/distro/target/distro/*.omod /opt/openmrs/modules

rm /opt/openmrs/modules/bahmnicore*
ln -s /bahmni-code/bahmni-core/bahmnicore-omod/target/*.omod /opt/openmrs/modules

rm /opt/openmrs/modules/openelis-atomfeed-client*
ln -s /bahmni-code/bahmni-core/openmrs-elis-atomfeed-client-omod/target/*.omod /opt/openmrs/modules

rm /opt/openmrs/modules/reference-data*.omod
ln -s /bahmni-code/bahmni-core/reference-data/omod/target/*.omod /opt/openmrs/modules

chown -h ${USER}:${USER} /opt/openmrs/modules/*