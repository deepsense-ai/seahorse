#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes base docker images for sessionmanager (Spark+Mesos)


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

./jenkins/scripts/checkout-submodules.sh

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
GIT_SHA=`git rev-parse HEAD`

( # build and publish deepsense-spark
cd deployment/docker

./build-local-docker.sh ../spark-docker/ deepsense-spark
./publish-local-docker.sh deepsense-spark $SEAHORSE_BUILD_TAG
)

( # Use just built `deepsense-spark` as a base image for following `mesos-spark-docker`
cd deployment/mesos-spark-docker

rm -f Dockerfile
sed "s|\${BASE_IMAGE_TAG}|$GIT_SHA|g" Dockerfile.template >> Dockerfile
)

( # build and publish deepsense-mesos-spark
cd deployment/docker

./build-local-docker.sh ../mesos-spark-docker/ deepsense-mesos-spark
./publish-local-docker.sh deepsense-mesos-spark $SEAHORSE_BUILD_TAG
)
