#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

set -e

# Settings
COMPOSE_FILE="http://artifactory.deepsense.codilime.com:8081/artifactory/deepsense-seahorse-docker-compose-snapshot/io/deepsense/docker-compose/latest/docker-compose.yml"
VAGRANT_BOX_NAME="seahorse-vm"
PUBLISH_DIR="../image_publication"

# Download docker-compose config file
cd ../deployment/vagrant_with_docker
rm -f docker-compose.yml
wget $COMPOSE_FILE

# Create Vagrant box
vagrant destroy -f $VAGRANT_BOX_NAME &>destroy.log
vagrant up $VAGRANT_BOX_NAME &>install.log
rm -f $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
vagrant package --output $PUBLISH_DIR/$VAGRANT_BOX_NAME.box &>package.log
vagrant destroy -f $VAGRANT_BOX_NAME &>destroy.log
