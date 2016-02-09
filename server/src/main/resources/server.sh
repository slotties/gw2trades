#!/bin/bash

CONFIG=$1
LOG_DIR=$2

if [ ! -f "$CONFIG" ]; then
    echo "Could not find $CONFIG"
    exit 1
fi
if [ ! -d "$LOG_DIR" ]; then
    echo "Could not find log directory $LOG_DIR"
    exit 1
fi

SERVER_LOG="$LOG_DIR/server_`date +%Y-%m-%d_%H-%M-%S`.log"
SERVER_GC_LOG="$LOG_DIR/server_`date +%Y-%m-%d_%H-%M-%S`.gc"
JAVA_OPTS="-Xms2g -Xmx2g -XX:+PrintGC -XX:+PrintGCDateStamps -Xloggc:$SERVER_GC_LOG -XX:+UseG1GC"

LIB_DIR=`dirname "$0"`/lib
CP=.
for i in $LIB_DIR/*.jar; do
    CP=$CP:$i
done


echo Logging to $SERVER_LOG and $SERVER_GC_LOG
java $JAVA_OPTS -cp $CP io.vertx.core.Launcher run gw2trades.server.Server -conf $CONFIG > $SERVER_LOG 2>&1
