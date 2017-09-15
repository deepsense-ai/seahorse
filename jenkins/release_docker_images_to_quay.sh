#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Tags latest dockers with SEAHORSE_BUILD_TAG and releases docker-compose.yml
#
# Usage: `jenkins/release_docker_images_to_quay.sh SEAHORSE_BUILD_TAG` from deepsense-backend catalog

set -e

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
QUAY_REGISTRY="quay.io/deepsense_io"
SEAHORSE_BUILD_TAG=$1
GIT_BRANCH="master"

echo "This script assumes it is run from deepsense-backend directory"
echo "Release SEAHORSE_BUILD_TAG $SEAHORSE_BUILD_TAG"

TMP_FILE=$(mktemp /tmp/dc-tmp.XXXXXX)
deployment/docker-compose/docker-compose.py --generate-only --yaml-file $TMP_FILE
DOCKER_IMAGES=(`cat $TMP_FILE | grep image: | cut -d":" -f 2 | rev | cut -d"/" -f 1 | rev | tr " " "\n"`)
rm $TMP_FILE

for DOCKER_IMAGE in ${DOCKER_IMAGES[*]}
do
  echo "************* Releasing docker image for project $DOCKER_IMAGE"

  LATEST_IMAGE_DEEPSENSE=$DEEPSENSE_REGISTRY/$DOCKER_IMAGE:$SEAHORSE_BUILD_TAG

  QUAY_IMAGE=$QUAY_REGISTRY/$DOCKER_IMAGE:$SEAHORSE_BUILD_TAG

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
