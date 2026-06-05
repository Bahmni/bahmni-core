#!/bin/sh -x

TEMP_LOCATION=/tmp/deploy_bahmni_core
USER=bahmni_support
#USER=jss
OMOD_LOCATION=/opt/openmrs/modules

sudo rm -f $OMOD_LOCATION/bahmnicore*.omod
sudo rm -f $OMOD_LOCATION/openelis-atomfeed-client*.omod
sudo rm -f $OMOD_LOCATION/reference-data*.omod

sudo su - $USER -c "sudo cp -f $TEMP_LOCATION/* $OMOD_LOCATION"
