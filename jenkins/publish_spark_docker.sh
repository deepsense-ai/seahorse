#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes spark-docker image

cd `dirname $0`"/../"

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
GIT_SHA=`git rev-parse HEAD`

cd deployment/docker

./build-local-docker.sh ../spark-docker/ deepsense-spark
./publish-local-docker.sh deepsense-spark $SEAHORSE_BUILD_TAG