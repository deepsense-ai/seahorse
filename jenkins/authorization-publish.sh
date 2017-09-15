#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish authorization docker
# $SEAHORSE_BUILD_TAG required for deployment
#
# Example usage from jenkins:
# ./jenkins/authorization-publish.sh

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../authorization-docker/ deepsense-authorization
./publish-local-docker.sh deepsense-authorization