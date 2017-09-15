#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-sessionmanager docker
# $SEAHORSE_BUILD_TAG required for deployment

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

SBT_OPTS="-XX:MaxPermSize=4G" \
  sbt clean sessionmanager/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-sessionmanager $SEAHORSE_BUILD_TAG
