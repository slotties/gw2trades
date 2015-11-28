#!/bin/bash

set CLASSPATH=

for i in lib/*; do
        CLASSPATH=$CLASSPATH:$i
done

LOG="log/importer_`date +%Y-%m-%d:%H:%M:%S`"

mkdir ./log
echo Logging to $LOG
java -cp $CLASSPATH gw2trades.importer.Main > $LOG  2>&1
