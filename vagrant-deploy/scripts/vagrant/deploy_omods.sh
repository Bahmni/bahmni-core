# This Source Code Form is subject to the terms of the Mozilla Public License,
# v. 2.0. If a copy of the MPL was not distributed with this file, You can
# obtain one at https://www.bahmni.org/license/mplv2hd.
#
# Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
# graphic logo is a trademark of OpenMRS Inc.

#!/bin/sh -x

TEMP_LOCATION=/tmp/deploy_bahmni_core
USER=bahmni_support
#USER=jss
OMOD_LOCATION=/opt/openmrs/modules

sudo rm -f $OMOD_LOCATION/bahmnicore*.omod
sudo rm -f $OMOD_LOCATION/openelis-atomfeed-client*.omod
sudo rm -f $OMOD_LOCATION/reference-data*.omod

sudo su - $USER -c "sudo cp -f $TEMP_LOCATION/* $OMOD_LOCATION"
