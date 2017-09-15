#!/bin/bash -ex
#
# Copyright (c) 2016, CodiLime Inc.
# Build and publish deepsense-proxy docker
# $SEAHORSE_BUILD_TAG required for deployment 

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

# Enter main directory
cd `dirname $0`"/../"

cd docker
./build-local-docker.sh ../proxy/ deepsense-proxy 
./publish-local-docker.sh ../proxy/ deepsense-proxy $SEAHORSE_BUILD_TAG


