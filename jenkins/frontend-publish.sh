#!/bin/bash -ex

# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-frontend docker

# Enter main directory
cd `dirname $0`"/../"

NPM_REGISTRY_URL="https://registry.npmjs.org"
npm set registry $NPM_REGISTRY_URL

cat ~/.npmrc
pwd

cd docker
./build-local-docker.sh ./ deepsense-frontend
./publish-local-docker.sh deepsense-frontend $SEAHORSE_BUILD_TAG
