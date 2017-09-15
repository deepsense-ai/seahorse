#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Publishes docker image from given project path and name.
#
# Example usage: ./publish-local-docker.sh deepsense-frontend SEAHORSE_BUILD_TAG

set -ex

PROJECT_NAME=$1
SEAHORSE_BUILD_TAG=$2 # Optional

GIT_SHA=`git rev-parse HEAD`
DOCKER_IMAGE=`docker images | grep $PROJECT_NAME | grep $GIT_SHA | head -1 | awk '{ print $3 }'`

GIT_BRANCH=`git branch | grep '*' | awk '{ print $2 }'`
if [ ! -z $GIT_TAG ]; then
  GIT_BRANCH="$GIT_TAG"
fi

echo $DOCKER_IMAGE

if [ -z $GIT_BRANCH ]; then
  echo ">>> Cannot get Git branch."
  exit 4
fi

# Settings
DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com"
NAMESPACE="deepsense_io"

docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA
docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$GIT_SHA

if [ ! -z "$GIT_BRANCH" ]; then
  # TODO When run locally will always override $BRANCH-latest. Add check whether it is truly tip of remote branch.
  TAG_BRANCH_LATEST="$GIT_BRANCH-latest"
  docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$TAG_BRANCH_LATEST
  docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$TAG_BRANCH_LATEST
fi

if [ ! -z "$SEAHORSE_BUILD_TAG" ]; then
  echo "SEAHORSE_BUILD_TAG is defined. Publishing with tag $SEAHORSE_BUILD_TAG"
  docker tag $DOCKER_IMAGE $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG
  docker push $DEEPSENSE_REGISTRY/$NAMESPACE/$PROJECT_NAME:$SEAHORSE_BUILD_TAG
fi

# Clean local images
echo ">>> Removing local images"
docker rmi -f $DOCKER_IMAGE
