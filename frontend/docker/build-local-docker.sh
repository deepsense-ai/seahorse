#!/bin/bash
# Copyright 2016, deepsense.ai
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


set -e

# Check if number of parameters is correct
if [ $# != 2 ]; then
  echo ">>> Exactly two parameters must be provided."
  exit 1;
fi

PROJECT_PATH=$1
PROJECT_NAME=$2
GIT_BRANCH=`git branch | grep '*' | awk '{ print $2 }'`
if [ ! -z $GIT_TAG ]; then
  GIT_BRANCH="$GIT_TAG"
fi

GIT_SHA=`git rev-parse HEAD`

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

# Build project if needed
if [ -f ../build.sh ]; then
  echo ">>> Building $PROJECT_NAME"
  (cd ../; ./build.sh)
  # Copy build
  echo ">>> Copying build"
  cp -r ../dist .
fi

# Build and tag docker image
echo ">>> Building docker and tagging it as latest"
docker build -t "$PROJECT_NAME:$GIT_SHA" .
