#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Builds docker with deepsense-frontend

echo "Removing old build"
[ -d build ] && rm -rf build

echo "Building deepsense-frontend"
(cd ../; ./build.sh)

echo "Copying build"
cp -r ../build .

echo "Building docker"
docker build -t deepsense-frontend .
