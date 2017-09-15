#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish docker

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

sbt clean datasourcemanager/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-datasourcemanager
