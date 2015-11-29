#!/bin/bash

LOG_DIR=$1
set CLASSPATH=

for i in `dirname $0`/lib/*; do
        CLASSPATH=$CLASSPATH:$i
done

LOG="$LOG_DIR/importer_`date +%Y-%m-%d:%H:%M:%S`"

echo Logging to $LOG
java -cp $CLASSPATH gw2trades.importer.Main > $LOG  2>&1
