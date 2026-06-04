#!/bin/bash 

#####################################################################################################
# This script can be used to call functions which will execute a command in your vagrant box. 
# -c option will be used to pass a command
# -f option will be used to pass a full qualified file that contains commands
#
# It can also be used to SCP into the vagrant box
#####################################################################################################

MACHINE_IP=192.168.33.10
KEY_FILE=~/.vagrant.d/insecure_private_key
TIMEOUT="-o ConnectTimeout=5"

function run_in_vagrant {
    
    if [ "$1" == "-c" ]; then
		ssh vagrant@$MACHINE_IP -i $KEY_FILE $TIMEOUT "$2"
	elif [ "$1" == "-f" ]; then
		ssh vagrant@$MACHINE_IP -i $KEY_FILE $TIMEOUT < "$2"
	fi

}

# $1: Source  $2: Dest
function scp_to_vagrant {
    scp  -i $KEY_FILE $TIMEOUT $1 vagrant@$MACHINE_IP:$2
}