#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish docker
# $SEAHORSE_BUILD_TAG required for deployment

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

cd `dirname $0`"/../"

sbt clean schedulingmanager/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-schedulingmanager $SEAHORSE_BUILD_TAG
