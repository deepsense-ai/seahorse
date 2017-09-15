#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-notebooks docker
#
# Example usage from jenkins:
# ./jenkins/notebooks-publish.sh

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../../remote_notebook/ deepsense-notebooks
./publish-local-docker.sh deepsense-notebooks
