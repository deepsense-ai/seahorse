#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

# This script is run inside of Vagrant image. Do not run it manually!

set -e

SYNCED_FOLDER="/vagrant"
RELEASE_SYNCED_FOLDER="/resources/data"

cd $SYNCED_FOLDER

mkdir -p $RELEASE_SYNCED_FOLDER
cp docker-compose.yml /resources

# Pull Seahorse docker-images
DOCKER_IMAGES=(`cat /resources/docker-compose.yml | grep image: | cut -d":" -f 2 | rev | cut -d"/" -f 1 | rev | tr " " "\n"`)
for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  echo "Loading $DOCKER_IMAGE.tar"
  docker load --input $DOCKER_IMAGE.tar
done
