#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds docker image from given project path and name.
#
# Example usage: ./build-docker.sh ../../remote_notebook/ deepsense-notebooks

set -e

# Check if number of parameters is correct
if [ $# != 2 ]; then
  echo ">>> Exactly two parameters must be provided."
  exit 1
fi

PROJECT_PATH=$1
PROJECT_NAME=$2

TAG_LATEST="$PROJECT_NAME:local-image-latest"

# Validate input parameters
if [ ! -d $PROJECT_PATH ]; then
  echo ">>> $PROJECT_PATH does not exist or is not a directory."
  exit 2
fi

if [ ! -f $PROJECT_PATH/Dockerfile ]; then
  echo ">>> No Dockerfile in given location."
  exit 3
fi

cd $PROJECT_PATH

# Build and tag docker image
echo ">>> Building docker and tagging it as latest"
docker build -t "$TAG_LATEST" .

