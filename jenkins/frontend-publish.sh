#!/bin/bash -ex

# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-frontend docker
# $SEAHORSE_BUILD_TAG required for deployment 

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

# Enter main directory
cd `dirname $0`"/../"

NPM_REGISTRY_URL="https://registry.npmjs.org"
npm set registry $NPM_REGISTRY_URL

cat ~/.npmrc
pwd

cd docker
./build-local-docker.sh ./ deepsense-frontend
./publish-local-docker.sh ./ deepsense-frontend $SEAHORSE_BUILD_TAG
