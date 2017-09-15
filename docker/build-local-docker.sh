#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds docker image from given project path and name.
#
# Example usage: ./build-docker.sh ../deepsense-frontend/docker deepsense-frontend

set -e

# Check if number of parameters is correct
if [ $# != 2 ]; then
  echo ">>> Exactly two parameters must be provided."
  exit 1;
fi

PROJECT_PATH=$1
PROJECT_NAME=$2
GIT_BRANCH=`git branch | grep '*' | awk '{ print $2 }'`
if [ ! -z $BRANCH ]; then
  GIT_BRANCH="$BRANCH"
fi

TAG_LATEST="$PROJECT_NAME:$GIT_BRANCH-latest"

# Validate input parameters
if [ ! -d $PROJECT_PATH ]; then
  echo ">>> $PROJECT_PATH does not exist or is not a directory."
  exit 2
fi

if [ ! -f $PROJECT_PATH/Dockerfile ]; then
  echo ">>> No Dockerfile in given location."
  exit 3
fi

if [ -z $GIT_BRANCH ]; then
  echo ">>> Cannot get Git branch."
  exit 4
fi

cd $PROJECT_PATH

# Remove old build
echo ">>> Removing old build"
[ -d build ] && rm -rf build

# Build project if needed
if [ -f ../build.sh ]; then
  echo ">>> Building $PROJECT_NAME"
  (cd ../; ./build.sh)
  # Copy build
  echo ">>> Copying build"
  cp -r ../build .
fi

# Build and tag docker image
echo ">>> Building docker and tagging it as latest"
docker build -t "$TAG_LATEST" .
