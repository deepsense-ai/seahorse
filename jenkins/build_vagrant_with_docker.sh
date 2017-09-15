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

echo "Save docker images to files"
DOCKER_IMAGES=(`cat docker-compose.yml | grep image: | cut -d":" -f 2 | cut -d"/" -f 3 | tr " " "\n"`)
for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  echo "Save docker image to $DOCKER_IMAGE.tar"
  DOCKER_IMAGE_FULL=$DEEPSENSE_REGISTRY/$DOCKER_IMAGE:$SEAHORSE_BUILD_TAG
  rm -f $DOCKER_IMAGE.tar
  docker save --output $DOCKER_IMAGE.tar $DOCKER_IMAGE_FULL
done

# Create Vagrant box
echo "Destroy Vagrant machine (1)"
vagrant destroy -f $VAGRANT_BOX_NAME
echo "Create Vagrant machine"
vagrant up $VAGRANT_BOX_NAME
echo "Export Vagrant machine to file"
rm -f $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
vagrant package --output $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
echo "Destroy Vagrant machine (2)"
vagrant destroy -f $VAGRANT_BOX_NAME
