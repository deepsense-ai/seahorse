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

DOCKER_IMAGES=(`cat deployment/docker-compose/docker-compose.tmpl.yml | grep image: | cut -d":" -f 2 | rev | cut -d"/" -f 1 | rev | tr " " "\n"`)
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

  # Clean local images
  echo ">>> removing local image"

  # Comment out line below in dev mode to keep cached images
  docker rmi -f $LATEST_IMAGE_DEEPSENSE

done

echo "Generating docker compose file with docker images tagged with $SEAHORSE_BUILD_TAG"

ARTIFACT_NAME_TMPL="docker-compose.yml.tmpl"
ARTIFACT_NAME="docker-compose.yml"

DOCKER_COMPOSE_TMPL="deployment/docker-compose/docker-compose.tmpl.yml"
rm -f $ARTIFACT_NAME_TMPL
rm -f $ARTIFACT_NAME
sed 's|\$DOCKER_REPOSITORY|'"$QUAY_REGISTRY"'|g ; s|\$DOCKER_TAG|'"$SEAHORSE_BUILD_TAG"'|g' $DOCKER_COMPOSE_TMPL >> $ARTIFACT_NAME_TMPL
deployment/docker-compose/prepare_docker-compose $ARTIFACT_NAME_TMPL $ARTIFACT_NAME

echo 'Sending & $ARTIFACT_NAME to snapshot artifactory'
source jenkins/publish_to_artifactory_function.sh
publish_to_artifactory $ARTIFACT_NAME seahorse-distribution/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}
