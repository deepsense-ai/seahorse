#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Run frontend unit tests


# Enter main directory
cd `dirname $0`"/../"

pwd
ls -l

NPM_REGISTRY_URL="https://registry.npmjs.org"
npm set registry $NPM_REGISTRY_URL

./run_tests.sh

