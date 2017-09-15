#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish deepsense-h2 docker
# $SEAHORSE_BUILD_TAG required for deployment
# $GIT_TAG requred for deployment
#
# Example usage from jenkins:
# ./jenkins/h2-docker-publish.sh

./jenkins/scripts/checkout-submodules.sh

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
GIT_TAG="${GIT_TAG?Need to set GIT_TAG. For example export GIT_TAG=master}"

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

cd deployment/h2-docker
docker build -t docker-repo.deepsense.codilime.com/deepsense_io/deepsense-h2:$SEAHORSE_BUILD_TAG -t docker-repo.deepsense.codilime.com/deepsense_io/deepsense-h2:$GIT_TAG-latest .

docker push docker-repo.deepsense.codilime.com/deepsense_io/deepsense-h2:$SEAHORSE_BUILD_TAG
docker push docker-repo.deepsense.codilime.com/deepsense_io/deepsense-h2:$GIT_TAG-latest
