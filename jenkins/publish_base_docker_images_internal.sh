#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes base docker images for sessionmanager (Spark+Mesos)


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

./jenkins/scripts/checkout-submodules.sh

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo "Usage: jenkins/publish_base_docker_images_internal.sh SEAHORSE_BUILD_TAG"
  exit 1
fi

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
SEAHORSE_BUILD_TAG=$1


(
echo "Building and publishing deepsense-spark:$SEAHORSE_BUILD_TAG"
cd deployment/spark-docker
docker build -t $DEEPSENSE_REGISTRY/deepsense-spark:$SEAHORSE_BUILD_TAG .
docker push $DEEPSENSE_REGISTRY/deepsense-spark:$SEAHORSE_BUILD_TAG
)

(
echo "Building and publishing deepsense-mesos-spark:$SEAHORSE_BUILD_TAG"
cd deployment/mesos-spark-docker
docker build -t $DEEPSENSE_REGISTRY/deepsense-mesos-spark:$SEAHORSE_BUILD_TAG .
docker push $DEEPSENSE_REGISTRY/deepsense-mesos-spark:$SEAHORSE_BUILD_TAG
)
