#!/bin/bash

set CLASSPATH=

for i in `dirname $0`/lib/*; do
        CLASSPATH=$CLASSPATH:$i
done

java -cp $CLASSPATH gw2trades.importer.Main
