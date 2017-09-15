#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds docker with deepsense-proxy and pushes it the repository

TIMESTAMP=`date +"%d%m%Y-%H%M%S"`
COMMIT_HASH=`git rev-parse HEAD`
NAME="deepsense-proxy"
DOCKER_REGISTRY="docker-registry.intra.codilime.com"
TAG_VERSION="$DOCKER_REGISTRY/tap/$NAME:tap-$TIMESTAMP-$COMMIT_HASH"
TAG_LATEST="$DOCKER_REGISTRY/tap/$NAME:latest"

echo "Building docker $NAME and tagging it for repository $DOCKER_REGISTRY"
docker build -t "$TAG_VERSION" -t "$TAG_LATEST" .

echo "Pushing docker $NAME to repository $DOCKER_REGISTRY"
docker push $TAG_VERSION
docker push $TAG_LATEST
