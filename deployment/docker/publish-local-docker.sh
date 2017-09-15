#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Publishes docker image from given project path and name.

set -ex

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo ">>> All required parameters must be provided."
  echo "Usage: ./publish-local-docker.sh deepsense-notebooks"
  echo "You can optionally set SEAHORSE_BUILD_TAG env variable to additionaly publish image tagged"
  echo "with this value. It can be used to publish docker image with any arbitrary synthetic tag."
  echo "It might be used to correlate few docker images build within a CI pipeline."
  exit 1
fi

PROJECT_NAME=$1

# Cannot use `local-image-latest` as grepped string, because SBT-built dockers won't have it in tag
DOCKER_IMAGE=`docker images | grep $PROJECT_NAME | grep "latest" | head -1 | awk '{ print $3 }'`

echo "Docker image for tagging and publishing:"
echo $DOCKER_IMAGE

if [ -z $DOCKER_IMAGE ]; then
  echo ">>> No local images for project $PROJECT_NAME."
  exit 3
fi

# Settings
DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com"
NAMESPACE="deepsense_io"

# Tag docker image
echo ">>> Tagging docker image and pushing docker to repository $DEEPSENSE_REGISTRY"

GIT_SHA=`git rev-parse HEAD`
docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA

if [ ! -z "$SEAHORSE_BUILD_TAG" ]; then
  echo "SEAHORSE_BUILD_TAG is defined. Publishing with tag $SEAHORSE_BUILD_TAG"
  docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG
  docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG
fi

GIT_BRANCH=`git symbolic-ref --short -q HEAD || echo ""` # it fails if not on branch
if [ ! -z "$GIT_BRANCH" ]; then
  git fetch origin $GIT_BRANCH

  if [ "$GIT_SHA" = "$(git rev-parse origin/$GIT_BRANCH)" ]
  then
    echo "This GIT_SHA is also tip of the origin/$GIT_BRANCH! Publishing $GIT_BRANCH-latest image"
    docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_BRANCH-latest
    docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_BRANCH-latest
  fi
fi