#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes base docker images for sessionmanager (Spark+Mesos)
#
# Usage: `jenkins/publish_base_docker_images_internal.sh VERSION` from deepsense-backend catalog

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo ">>> Exactly one parameter must be provided."
  exit 1
fi


DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
VERSION=$1

(
echo "Building and publishing deepsense-spark:$VERSION"
cd deployment/spark-docker
docker build -t $DEEPSENSE_REGISTRY/deepsense-spark:$VERSION .
docker push $DEEPSENSE_REGISTRY/deepsense-spark:$VERSION
)

(
echo "Building and publishing deepsense-mesos-spark:$VERSION"
cd deployment/mesos-spark-docker
docker build -t $DEEPSENSE_REGISTRY/deepsense-mesos-spark:$VERSION .
docker push $DEEPSENSE_REGISTRY/deepsense-mesos-spark:$VERSION
)
