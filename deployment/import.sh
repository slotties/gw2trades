#!/bin/bash

IMPORTER_SH=$1
LOG_DIR=$2
INDEX_DIR=$3

if [ ! -f "$IMPORTER_SH" ]; then
    echo "Could not find $IMPORTER_SH"
    exit 1
fi
if [ ! -d "$LOG_DIR" ]; then
    echo "Could not find log directory $LOG_DIR"
    exit 1
fi
if [ ! -d "$INDEX_DIR" ]; then
    echo "Could not find index directory $INDEX_DIR"
    exit 1
fi

IMPORT_LOG="$LOG_DIR/import_`date +%Y-%m-%d:%H:%M:%S`.log"

echo Logging to $IMPORT_LOG
bash $IMPORTER_SH "$INDEX_DIR" > $IMPORT_LOG 2>&1
