#!/bin/bash

SERVER_JAR=$1
LOG_DIR=$2
INDEX_DIR=$3

if [ ! -f "$SERVER_JAR" ]; then
    echo "Could not find $SERVER_JAR"
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

SERVER_LOG="$LOG_DIR/server_`date +%Y-%m-%d_%H-%M-%S`.log"
SERVER_GC_LOG="$LOG_DIR/server_`date +%Y-%m-%d_%H-%M-%S`.gc"
JAVA_OPTS="-Xms2g -Xmx2g -XX:+PrintGC -XX:+PrintGCDateStamps -Xloggc:$SERVER_GC_LOG -XX:+UseG1GC"

echo Logging to $SERVER_LOG and $SERVER_GC_LOG
java $JAVA_OPTS -jar $SERVER_JAR --server.port=8080 --server.address=localhost --index.dir=$INDEX_DIR > $SERVER_LOG 2>&1
