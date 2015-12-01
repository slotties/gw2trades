#!/bin/bash

SRC_DIR=$1
APP_DIR=$2
VERSION=$3

if [ "$SRC_DIR" == "" ] || [ "$APP_DIR" == "" ] || [ "$VERSION" == "" ]; then
    echo "Syntax: deploy.sh SRC_DIR APP_DIR VERSION"
    exit 1
fi

rm -rf $APP_DIR/import
unzip $SRC_DIR/import/build/distributions/gw2trades-importer-$VERSION.zip -d $APP_DIR/
mv $APP_DIR/gw2trades-importer-$VERSION $APP_DIR/import
chmod u+x $APP_DIR/import/*.sh

rm -rf $APP_DIR/server/*
cp -v $SRC_DIR/server/build/libs/server-$VERSION.jar $APP_DIR/server
