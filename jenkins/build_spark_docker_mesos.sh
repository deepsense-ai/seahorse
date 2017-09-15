#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes mesos-spark-docker image

cd `dirname $0`"/../"

GIT_SHA=`git rev-parse HEAD`

( # Materialize dockerfile.template with proper base image using GIT_SHA
cd deployment/mesos-spark-docker
rm -f Dockerfile
sed "s|\${BASE_IMAGE_TAG}|$GIT_SHA|g" Dockerfile.template >> Dockerfile
)

( # build and publish deepsense-mesos-spark
cd jenkins/scripts
./build-local-docker.sh ../../deployment/mesos-spark-docker/ deepsense-mesos-spark
)
