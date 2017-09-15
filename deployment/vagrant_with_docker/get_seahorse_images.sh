#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

# This script is run inside of Vagrant image. Do not run it manually!

set -e

SYNCED_FOLDER="/vagrant"
RELEASE_SYNCED_FOLDER="/resources/data"

cd $SYNCED_FOLDER

mkdir -p $RELEASE_SYNCED_FOLDER
cp docker-compose.yml /resources
cd /resources

# Pull Seahorse docker-images
DOCKER_IMAGES=("deepsense-sessionmanager" "deepsense-workflowmanager" "deepsense-notebooks" "deepsense-rabbitmq" "deepsense-h2" "deepsense-frontend" "deepsense-proxy")
for DOCKER_IMAGE in "${DOCKER_IMAGES[@]}"
do
  docker load --input $DOCKER_IMAGE.tar
done
