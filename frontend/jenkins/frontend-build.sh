#!/bin/bash -ex

# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-frontend docker

# Enter main directory
cd `dirname $0`"/../"

export NPM_REGISTRY_URL="https://registry.npmjs.org"
npm set registry $NPM_REGISTRY_URL

cat ~/.npmrc
pwd

cd docker
./build-local-docker.sh ./ deepsense-frontend
