#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.
#
# Usage ./build_vagrant_with_docker.sh SEAHORSE_BUILD_TAG

set -e

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo ">>> Exactly one parameters must be provided."
  exit 1
fi


# Settings
SEAHORSE_BUILD_TAG=$1
ARTIFACT_NAME="docker-compose-internal.yml"
COMPOSE_FILE="http://artifactory.deepsense.codilime.com:8081/artifactory/seahorse-distribution/io/deepsense/$SEAHORSE_BUILD_TAG/dockercompose/$ARTIFACT_NAME"
VAGRANT_BOX_NAME="seahorse-vm"
PUBLISH_DIR="../image_publication"

# Download docker-compose config file
cd ../deployment/vagrant_with_docker
rm -f $ARTIFACT_NAME
wget $COMPOSE_FILE
mv $ARTIFACT_NAME docker-compose.yml

# Save docker images to files
DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"

docker-compose pull

DOCKER_IMAGES=("deepsense-sessionmanager" "deepsense-workflowmanager" "deepsense-notebooks" "deepsense-rabbitmq" "deepsense-h2" "deepsense-frontend" "deepsense-proxy")
for DOCKER_IMAGE in "${DOCKER_IMAGES[@]}"
do
  DOCKER_IMAGE_FULL=$DEEPSENSE_REGISTRY/$DOCKER_IMAGE:$SEAHORSE_BUILD_TAG
  docker save $DOCKER_IMAGE_FULL --output $DOCKER_IMAGE.tar
done

# Create Vagrant box
vagrant destroy -f $VAGRANT_BOX_NAME
vagrant up $VAGRANT_BOX_NAME
rm -f $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
vagrant package --output $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
vagrant destroy -f $VAGRANT_BOX_NAME
