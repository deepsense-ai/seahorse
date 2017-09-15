#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Prints API_VERSION

VERSION_SBT_PATH=`dirname $0`"/../../version.sbt"
API_VERSION=`cat "$VERSION_SBT_PATH" | sed -n  's/version in ThisBuild := "\([0-9.]\+\).*/\1/p'`
if [ -z "$API_VERSION" ]; then
	echo "Couldn't gather API_VERSION from $VERSION_SBT_PATH"
	exit 3
fi

echo $API_VERSION