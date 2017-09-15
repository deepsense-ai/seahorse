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


TIMESTAMP=`date +"%d%m%Y-%H%M%S"`
COMMIT_HASH=`git rev-parse HEAD`
NAME="deepsense-frontend"
DOCKER_REGISTRY="docker-repo.deepsense.codilime.com"
TAG_VERSION="$DOCKER_REGISTRY/tap/$NAME:tap-$TIMESTAMP-$COMMIT_HASH"
TAG_LATEST="$DOCKER_REGISTRY/tap/$NAME:latest"

echo "Removing old build"
[ -d dist ] && rm -rf dist

echo "Building $NAME"
(cd ../; ./build.sh)

echo "Copying build"
cp -r ../dist .

echo "Building docker and tagging it for repository $DOCKER_REGISTRY"
docker build -t "$TAG_VERSION" -t "$TAG_LATEST" .

echo "Pushing docker to repository $DOCKER_REGISTRY"
docker push $TAG_VERSION
docker push $TAG_LATEST
