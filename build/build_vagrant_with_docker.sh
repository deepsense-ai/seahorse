#!/bin/bash -ex
# Copyright 2017 deepsense.ai (CodiLime, Inc)
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
#


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"
ROOT_DIR=$(pwd)

# Check if number of parameters is correct
if [ $# != 1 ]; then
  echo "Usage: build/build_vagrant_with_docker.sh GIT_TAG"
  exit 1
fi

# Settings
GIT_TAG=$1
ARTIFACT_NAME="docker-compose.yml"
VAGRANT_BOX_NAME="seahorse-vm"
PUBLISH_DIR="../image_publication"

# Download docker-compose config file
cd deployment/vagrant_with_docker
rm -f $ARTIFACT_NAME


"$ROOT_DIR/build/build_docker_compose_internal.sh" $GIT_TAG
echo "Using $ROOT_DIR/$ARTIFACT_NAME"
mv $ROOT_DIR/docker-compose.yml $ARTIFACT_NAME

# Inside Vagrant we need Seahorse to listen on 0.0.0.0,
# so that Vagrant's port forwarding works. So, let's replace the host which
# proxy listens on.
"$ROOT_DIR/build/scripts/proxy_on_any_interface.py" $ARTIFACT_NAME

"$ROOT_DIR/build/manage-docker.py" -b --all

echo "Save docker images to files"
DOCKER_IMAGES=(`cat $ARTIFACT_NAME | grep image: | cut -d" " -f 6 | tr " " "\n"`)
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
echo "Export Vagrant machine to file $PUBLISH_DIR/$VAGRANT_BOX_NAME.box"
rm -f $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
vagrant package --output $PUBLISH_DIR/$VAGRANT_BOX_NAME.box
echo "Destroy Vagrant machine (2)"
vagrant destroy -f $VAGRANT_BOX_NAME


cd ../image_publication/
echo ""
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo "Path to builded image file: `pwd`/seahorse-vm.box"
echo "Image size: `du -h seahorse-vm.box|cut -f1`"
echo "++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++"
echo ""
