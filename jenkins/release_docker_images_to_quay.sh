#!/bin/bash
# Copyright 2016, deepsense.ai
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



# Tags latest dockers with SEAHORSE_BUILD_TAG and releases docker-compose.yml
#
# Usage: `jenkins/release_docker_images_to_quay.sh ARTIFACTORY_URL SEAHORSE_DISTRIBUTION_REPOSITORY SEAHORSE_BUILD_TAG`
#  from deepsense-backend catalog

set -e

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
QUAY_REGISTRY="quay.io/deepsense_io"

ARTIFACTORY_URL=$1
SEAHORSE_DISTRIBUTION_REPOSITORY=$2
SEAHORSE_BUILD_TAG=$3

echo "This script assumes it is run from deepsense-backend directory"
echo "Release SEAHORSE_BUILD_TAG $SEAHORSE_BUILD_TAG"

LINK_TO_DOCKER_COMPOSE_FILE=
TMP_FILE=$(mktemp /tmp/dc-tmp.XXXXXX)
wget \
  --output-document=$TMP_FILE \
  "${ARTIFACTORY_URL}/${SEAHORSE_DISTRIBUTION_REPOSITORY}/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/docker-compose-internal.yml"

DOCKER_IMAGES=(`cat $TMP_FILE | grep image: | cut -d":" -f 2,3 | rev | cut -d"/" -f 1 | rev | tr " " "\n"`)
rm $TMP_FILE

for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  echo "************* Releasing docker image for project $DOCKER_IMAGE"

  LATEST_IMAGE_DEEPSENSE=$DEEPSENSE_REGISTRY/$DOCKER_IMAGE
  DOCKER_IMAGE_NAME=`cut -d":" -f1 <<<$DOCKER_IMAGE`
  QUAY_IMAGE=$QUAY_REGISTRY/$DOCKER_IMAGE_NAME:$SEAHORSE_BUILD_TAG

  echo ">>> pulling docker image $LATEST_IMAGE_DEEPSENSE"
  docker pull $LATEST_IMAGE_DEEPSENSE

  docker tag $LATEST_IMAGE_DEEPSENSE $QUAY_IMAGE

  echo ">>> pushing to $QUAY_IMAGE"
  docker push $QUAY_IMAGE

done

echo "Generating docker compose file with docker images tagged with $SEAHORSE_BUILD_TAG"

ARTIFACT_NAME="docker-compose.yml"
rm -f $ARTIFACT_NAME

deployment/docker-compose/docker-compose.py --generate-only --yaml-file $ARTIFACT_NAME \
  -f $SEAHORSE_BUILD_TAG -b $SEAHORSE_BUILD_TAG --docker-repo $QUAY_REGISTRY

echo 'Sending & $ARTIFACT_NAME to snapshot artifactory'
source jenkins/publish_to_artifactory_function.sh
publish_to_artifactory $ARTIFACT_NAME seahorse-distribution/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}
