#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes mesos-spark-docker image

cd `dirname $0`"/../"

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
GIT_SHA=`git rev-parse HEAD`

( # Materialize dockerfile.template with proper base image using GIT_SHA
cd deployment/mesos-spark-docker
rm -f Dockerfile
sed "s|\${BASE_IMAGE_TAG}|$GIT_SHA|g" Dockerfile.template >> Dockerfile
)

( # build and publish deepsense-mesos-spark
cd deployment/docker
./build-local-docker.sh ../mesos-spark-docker/ deepsense-mesos-spark
./publish-local-docker.sh deepsense-mesos-spark $SEAHORSE_BUILD_TAG
)
