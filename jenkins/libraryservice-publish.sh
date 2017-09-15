#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-libraryservice docker
# $SEAHORSE_BUILD_TAG required for deployment
#
# Example usage from jenkins:
# ./jenkins/libraryservice-publish.sh

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

SBT_OPTS="-XX:MaxPermSize=4G" \
  sbt clean libraryservice/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-libraryservice
