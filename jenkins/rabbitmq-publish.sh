#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-rabbitmq docker
#
# Example usage from jenkins:
# ./jenkins/rabbitmq-publish.sh

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../rabbitmq/ deepsense-rabbitmq
./publish-local-docker.sh deepsense-rabbitmq
