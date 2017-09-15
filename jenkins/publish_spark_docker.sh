#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes spark-docker image

cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../spark-docker/ deepsense-spark
./publish-local-docker.sh deepsense-spark