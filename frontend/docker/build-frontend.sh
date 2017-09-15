#!/bin/bash

set -e

cd `dirname $0`

echo ">>> Building frontend"
(cd ../; ./build.sh)
# Copy build
echo ">>> Copying build"
cp -r ../dist .

# Build and tag docker image
GIT_SHA=`git rev-parse HEAD`

echo ">>> Building docker and tagging it as latest"
docker build -t "seahorse-frontend:$GIT_SHA" .
