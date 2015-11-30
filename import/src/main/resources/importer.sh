#!/bin/bash

CLASSPATH=
INDEX_DIR=$1

for i in `dirname $0`/lib/*; do
        CLASSPATH=$CLASSPATH:$i
done

java -cp $CLASSPATH -Dindex.dir=$INDEX_DIR gw2trades.importer.Main
