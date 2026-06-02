# This Source Code Form is subject to the terms of the Mozilla Public License,
# v. 2.0. If a copy of the MPL was not distributed with this file, You can
# obtain one at https://www.bahmni.org/license/mplv2hd.
#
# Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
# graphic logo is a trademark of OpenMRS Inc.

#!/bin/sh -x
PATH_OF_CURRENT_SCRIPT="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
source $PATH_OF_CURRENT_SCRIPT/../vagrant-deploy/scripts/vagrant/vagrant_functions.sh

set -e
$PATH_OF_CURRENT_SCRIPT/vagrant-deploy.sh

#invoke migration of openmrs core
run_in_vagrant -c "sudo su - bahmni -c 'cd /bahmni_temp/ && ./run-liquibase-openmrs.sh'"
#invoke migrations of bahmni core omods
run_in_vagrant -c "sudo su - bahmni -c 'cd /bahmni_temp/ && ./run-core-bahmni-modules-liquibase.sh'"
run_in_vagrant -c "sudo su - bahmni -c 'cd /bahmni_temp/ && ./run-openelis-atomfeed-client-liquibase.sh'"
