#!/bin/bash -ex
# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Build and publish vagrant image
# $SEAHORSE_BUILD_TAG required for deployment
# $API_VERSION requred for deployment

# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"
API_VERSION=`utils/api_version.py`

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
