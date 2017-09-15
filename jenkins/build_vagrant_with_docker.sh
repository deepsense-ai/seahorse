#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Builds Seahorse vagrant image that internally uses Seahorse docker images.


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"
ROOT_DIR=$(pwd)

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo "Usage: jenkins/build_vagrant_with_docker.sh SEAHORSE_BUILD_TAG"
  exit 1
fi

# Settings
SEAHORSE_BUILD_TAG=$1
ARTIFACT_NAME="docker-compose-internal.yml"
COMPOSE_FILE="http://artifactory.deepsense.codilime.com:8081/artifactory/seahorse-distribution/io/deepsense/$SEAHORSE_BUILD_TAG/dockercompose/$ARTIFACT_NAME"
VAGRANT_BOX_NAME="seahorse-vm"
PUBLISH_DIR="../image_publication"

# Download docker-compose config file
cd deployment/vagrant_with_docker
rm -f $ARTIFACT_NAME
wget $COMPOSE_FILE
mv $ARTIFACT_NAME docker-compose.yml

# Inside Vagrant we need Seahorse to listen on 0.0.0.0,
# so that Vagrant's port forwarding works. So, let's replace the host which
# proxy listens on.
"$ROOT_DIR/jenkins/scripts/proxy_on_any_interface.py" docker-compose.yml

docker-compose pull

echo "Save docker images to files"
DOCKER_IMAGES=(`cat docker-compose.yml | grep image: | cut -d" " -f 6 | tr " " "\n"`)
for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  # Strip docker repository and docker tag from image.
  IMAGE_FILE_NAME=`echo "$DOCKER_IMAGE" | cut -d "/" -f 3 | rev | cut -d ":" -f 2 | rev`
  echo "Save docker image to $IMAGE_FILE_NAME.tar"
  rm -f $IMAGE_FILE_NAME.tar
  docker save --output $IMAGE_FILE_NAME.tar $DOCKER_IMAGE
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
