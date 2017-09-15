#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds and publishes docker from given project path and name.
#
# Example usage: ./build-docker.sh ../../remote_notebook/ deepsense-notebooks

if [ $# != 2 ]; then
  echo "Exactly two parameters must be provided."
  exit 1;
fi

PROJECT_PATH=$1;
PROJECT_NAME=$2

if [ ! -d $PROJECT_PATH ]; then
  echo "$PROJECT_PATH does not exist or is not a directory."
  exit 2;
fi

if [ ! -f $PROJECT_PATH/Dockerfile ]; then
  echo "No Dockerfile in given location."
  exit 2;
fi

cd $PROJECT_PATH

TIMESTAMP=`date +"%d%m%Y-%H%M%S"`
COMMIT_HASH=`git rev-parse HEAD`
DOCKER_REGISTRY="docker-registry.intra.codilime.com"
TAG_VERSION="$DOCKER_REGISTRY/tap/$PROJECT_NAME:tap-$TIMESTAMP-$COMMIT_HASH"
TAG_LATEST="$DOCKER_REGISTRY/tap/$PROJECT_NAME:latest"

echo "Building docker and tagging it for repository $DOCKER_REGISTRY"
docker build -t "$TAG_VERSION" -t "$TAG_LATEST" .

echo "Pushing docker to repository $DOCKER_REGISTRY"
docker push $TAG_VERSION
docker push $TAG_LATEST

