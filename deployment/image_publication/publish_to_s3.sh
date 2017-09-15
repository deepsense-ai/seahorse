#!/bin/bash

# Copyright (c) 2016, CodiLime Inc.

VERSION=$1
if [ -z ${VERSION+x} ]; then VERSION=$(date +"Y-%m-%d_%H-%M-%S"); else echo "VERSION='$VERSION'"; fi

BOX_NAME="seahorse-vm.box"
RELEASE_PATH=workflowexecutor/seahorse/releases/${VERSION}

aws s3 cp ${BOX_NAME} s3://${RELEASE_PATH}/${BOX_NAME} --acl public-read

URL="https://s3.amazonaws.com/${RELEASE_PATH}/${BOX_NAME}"
sed "s#seahorsevm.vm.box_url = \"\"#seahorsevm.vm.box_url = \"${URL}\"#" Vagrantfile.template > Vagrantfile

aws s3 cp Vagrantfile s3://${RELEASE_PATH}/Vagrantfile --acl public-read

echo "PATH: https://s3.amazonaws.com/${RELEASE_PATH}/Vagrantfile"
