#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Publishes docker image from given project path and name.
#
# Example usage: ./publish-local-docker.sh ../deepsense-frontend/docker/ deepsense-frontend SEAHORSE_BUILD_TAG
# Note: SEAHORSE_BUILD_TAG can be generated using: SEAHORSE_BUILD_TAG=`date +%Y%m%d_%H%M%S`-$GIT_TAG

set -e

# Check if number of parameters is correct
if [ $# != 3 ]; then
  echo ">>> Exactly three parameters must be provided."
  exit 1
fi

PROJECT_PATH=$1;
PROJECT_NAME=$2
SEAHORSE_BUILD_TAG=$3
DOCKER_IMAGE=`docker images | grep $PROJECT_NAME | grep "latest" | head -1 | awk '{ print $3 }'`
GIT_BRANCH=`git branch | grep '*' | awk '{ print $2 }'`
if [ ! -z $GIT_TAG ]; then
  GIT_BRANCH="$GIT_TAG"
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
DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com"
NAMESPACE="deepsense_io"
TAG_VERSION="$PROJECT_NAME:$SEAHORSE_BUILD_TAG"
TAG_LATEST="$PROJECT_NAME:$GIT_BRANCH-latest"

# Tag docker image
echo ">>> Tagging docker image"
docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$TAG_VERSION
docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$TAG_LATEST

# Push built docker image
echo ">>> Pushing docker to repository $DEEPSENSE_REGISTRY"
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$TAG_VERSION
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$TAG_LATEST

# Clean local images
echo ">>> Removing local images"
docker rmi -f $DOCKER_IMAGE
