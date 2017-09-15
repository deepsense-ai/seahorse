#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Publishes docker image from given project path and name.

set -ex

# Check if number of parameters is correct
if [ $# != 2 ]; then
  echo ">>> All required parameters must be provided."
  echo "Usage: ./publish-local-docker.sh deepsense-notebooks SEAHORSE_BUILD_TAG"
  exit 1
fi

PROJECT_NAME=$1
SEAHORSE_BUILD_TAG=$2
# Cannot use `local-image-latest` as grepped string, because SBT-built dockers won't have it in tag
DOCKER_IMAGE=`docker images | grep $PROJECT_NAME | grep "latest" | head -1 | awk '{ print $3 }'`
if [ ! -z $GIT_TAG ]; then
  GIT_BRANCH="$GIT_TAG"
else
  GIT_BRANCH=`git symbolic-ref --short -q HEAD || echo ""` # it fails if not on branch
fi

echo "Docker image for tagging and publishing:"
echo $DOCKER_IMAGE

if [ -z $DOCKER_IMAGE ]; then
  echo ">>> No local images for project $PROJECT_NAME."
  exit 3
fi

# Settings
DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com"
NAMESPACE="deepsense_io"

GIT_SHA=`git rev-parse HEAD`

# Tag docker image
echo ">>> Tagging docker image and pushing docker to repository $DEEPSENSE_REGISTRY"
docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG

docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA

if [ ! -z "$GIT_BRANCH" ]; then
  docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_BRANCH-latest
  docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_BRANCH-latest
fi