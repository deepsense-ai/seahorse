#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-sessionmanager docker

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

SBT_OPTS="-XX:MaxPermSize=4G" \
  sbt clean sessionmanager/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-sessionmanager
