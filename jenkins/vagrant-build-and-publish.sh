#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish vagrant image
# $SEAHORSE_BUILD_TAG required for deployment
# $API_VERSION requred for deployment

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
API_VERSION="${API_VERSION?Need to set SEAHORSE_BUILD_TAG. For example export API_VERSION="1.3.0"}"

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

echo "Building Vagrant image"
# run from ./jenkins
cd jenkins
./build_vagrant_with_docker.sh $SEAHORSE_BUILD_TAG

echo "Publishing vagrant image"
cd ../deployment/image_publication/

echo ""
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "Path to builded image file: `pwd`/seahorse-vm.box"
echo "Image size: `du -h seahorse-vm.box|cut -f1`"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo ""


echo "Publishing with SEAHORSE_BUILD_TAG=$SEAHORSE_BUILD_TAG  API_VERSION=$API_VERSION"
sudo bash publish.sh $SEAHORSE_BUILD_TAG $API_VERSION
