#!/bin/bash

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


# This script is run inside of Vagrant image. Do not run it manually!

set -e

SYNCED_FOLDER="/vagrant"

cd $SYNCED_FOLDER

mkdir -p /resources/data
mkdir -p /resources/jars
mkdir -p /resources/spark_applications_logs
cp docker-compose.yml /resources

# Pull Seahorse docker-images
DOCKER_IMAGES=(`cat /resources/docker-compose.yml | grep image: | cut -d":" -f 2 | rev | cut -d"/" -f 1 | rev | tr " " "\n"`)
for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  echo "Loading $DOCKER_IMAGE.tar"
  cp $DOCKER_IMAGE.tar /home/vagrant
done
