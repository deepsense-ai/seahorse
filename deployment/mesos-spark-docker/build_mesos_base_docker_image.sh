#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Generates Dockerfile for base mesos image and builds it


# Set working directory to this script directory
# `dirname $0` gives folder containing script
cd `dirname $0`

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo "Usage: ./build_mesos_base_docker_image.sh SEAHORSE_BUILD_TAG"
  exit 1
fi

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
SEAHORSE_BUILD_TAG=$1

rm -f Dockerfile
sed "s|\${SEAHORSE_BUILD_TAG}|$SEAHORSE_BUILD_TAG|g" Dockerfile.template >> Dockerfile

docker build \
    -t $DEEPSENSE_REGISTRY/deepsense-mesos-spark:$SEAHORSE_BUILD_TAG \
    -t $DEEPSENSE_REGISTRY/deepsense-mesos-spark:latest \
    .
