#!/bin/bash

source ./conf/init.sh

# stop the instance
java -cp ./lib/stopper.jar com.seyhanproject.stopper.Stopper $http_port
if [ -f ./RUNNING_PID ]; then
   rm RUNNING_PID
fi
