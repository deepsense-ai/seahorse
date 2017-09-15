#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds docker with deepsense-frontend

TIMESTAMP=`date +"%d%m%Y-%H%M%S"`
COMMIT_HASH=`git rev-parse HEAD`
NAME="deepsense-frontend"
DOCKER_REGISTRY="docker-repo.deepsense.codilime.com"
TAG_VERSION="$DOCKER_REGISTRY/tap/$NAME:tap-$TIMESTAMP-$COMMIT_HASH"
TAG_LATEST="$DOCKER_REGISTRY/tap/$NAME:latest"

echo "Removing old build"
[ -d build ] && rm -rf build

echo "Building $NAME"
(cd ../; ./build.sh)

echo "Copying build"
cp -r ../build .

echo "Building docker and tagging it for repository $DOCKER_REGISTRY"
docker build -t "$TAG_VERSION" -t "$TAG_LATEST" .

echo "Pushing docker to repository $DOCKER_REGISTRY"
docker push $TAG_VERSION
docker push $TAG_LATEST
