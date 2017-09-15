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
docker-compose pull
