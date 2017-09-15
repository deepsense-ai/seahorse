#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-h2 docker
#
# Example usage from jenkins:
# ./jenkins/h2-docker-publish.sh

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../h2-docker/ deepsense-h2
./publish-local-docker.sh deepsense-h2