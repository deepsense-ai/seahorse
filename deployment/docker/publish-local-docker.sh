#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Publishes docker image from given project path and name.
#
# Example usage: ./publish-local-docker.sh ../../remote_notebook/ deepsense-notebooks

set -e

# Check if number of parameters is correct
if [ $# != 2 ]; then
  echo ">>> Exactly two parameters must be provided."
  exit 1
fi

PROJECT_PATH=$1;
PROJECT_NAME=$2
DOCKER_IMAGE=`docker images | grep $PROJECT_NAME | grep "latest" | head -1 | awk '{ print $3 }'`
GIT_BRANCH=`git branch | grep '*' | awk '{ print $2 }'`
if [ ! -z $BRANCH ]; then
  GIT_BRANCH="$BRANCH"
fi

echo $DOCKER_IMAGE

# Validation
if [ ! -d $PROJECT_PATH ]; then
  echo ">>> $PROJECT_PATH does not exist or is not a directory."
  exit 2
fi

if [ -z $DOCKER_IMAGE ]; then
  echo ">>> No local images for project $PROJECT_NAME."
  exit 3
fi

if [ -z $GIT_BRANCH ]; then
  echo ">>> Cannot get Git branch."
  exit 4
fi

cd $PROJECT_PATH

# Settings
TIMESTAMP=`date +"%d%m%Y-%H%M%S"`
COMMIT_HASH=`git rev-parse HEAD`
DOCKER_REGISTRY="docker-registry.intra.codilime.com"
QUAY_REGISTRY="quay.io"
CL_NAMESPACE="tap"
QUAY_NAMESPACE="intelseahorse"
TAG_VERSION="$PROJECT_NAME:$GIT_BRANCH-$TIMESTAMP-$COMMIT_HASH"
TAG_LATEST="$PROJECT_NAME:$GIT_BRANCH-latest"

# Tag docker image
echo ">>> Tagging docker image"
docker tag $DOCKER_IMAGE $DOCKER_REGISTRY/$CL_NAMESPACE/$TAG_VERSION
docker tag $DOCKER_IMAGE $DOCKER_REGISTRY/$CL_NAMESPACE/$TAG_LATEST
docker tag $DOCKER_IMAGE $QUAY_REGISTRY/$QUAY_NAMESPACE/$TAG_VERSION
docker tag $DOCKER_IMAGE $QUAY_REGISTRY/$QUAY_NAMESPACE/$TAG_LATEST

# Push built docker image
echo ">>> Pushing docker to repository $DOCKER_REGISTRY"
docker push $DOCKER_REGISTRY/$CL_NAMESPACE/$TAG_VERSION
docker push $DOCKER_REGISTRY/$CL_NAMESPACE/$TAG_LATEST

echo ">>> Pushing docker to repository $QUAY_REGISTRY"
docker push $QUAY_REGISTRY/$QUAY_NAMESPACE/$TAG_VERSION
docker push $QUAY_REGISTRY/$QUAY_NAMESPACE/$TAG_LATEST

# Clean local images
echo ">>> Removing local images"
docker rmi -f $DOCKER_IMAGE
