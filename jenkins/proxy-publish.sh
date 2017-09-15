#!/bin/bash -ex
#
# 
# Copyright (c) 2016, CodiLime Inc.
# Build and publish deepsense-proxy docker

cd `dirname $0`"/../"

cd deployment/docker
./build-local-docker.sh ../../proxy/ deepsense-proxy
./publish-local-docker.sh deepsense-proxy