#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Tags latest dockers with UUID and releases docker-compose.yml
#
# Usage: `jenkins/release_docker_images.sh` from deepsense-backend catalog

set -e

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
QUAY_REGISTRY="quay.io/deepsense_io"
UUID=`uuidgen`
GIT_BRANCH="master"

echo "This script assumes it is run from deepsense-backend directory"
echo "Release UUID $UUID"

DOCKER_IMAGES=("deepsense-sessionmanager" "deepsense-workflowmanager" "deepsense-notebooks" "deepsense-rabbitmq" "deepsense-h2" "deepsense-frontend" "deepsense-proxy")
for DOCKER_IMAGE in "${DOCKER_IMAGES[@]}"
do
  echo "************* Releasing docker image for project $DOCKER_IMAGE"

  LATEST_IMAGE_DEEPSENSE=$DEEPSENSE_REGISTRY/$DOCKER_IMAGE:$GIT_BRANCH-latest

  RELEASE_TAG="$DOCKER_IMAGE:$GIT_BRANCH-$UUID"
  QUAY_IMAGE=$QUAY_REGISTRY/$RELEASE_TAG

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

echo "Generating docker compose file with docker images tagged with $UUID"

ARTIFACT_NAME="docker-compose.yml"

DOCKER_COMPOSE_TMPL="deployment/docker-compose/docker-compose.tmpl.yml"
rm -f $ARTIFACT_NAME
sed 's|\$DOCKER_REPOSITORY|'"$QUAY_REGISTRY"'|g ; s|\$DOCKER_TAG|'"$UUID"'|g' $DOCKER_COMPOSE_TMPL >> $ARTIFACT_NAME

echo 'Sending docker-compose.yml to snapshot artifactory'

ARTIFACTORY_CREDENTIALS=$HOME/.artifactory_credentials

ARTIFACTORY_USER=`grep "user=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_PASSWORD=`grep "password=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_URL=`grep "host=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`

SNAPSHOT_REPOSITORY="deepsense-seahorse-docker-compose-snapshot"

REPOSITORY_URL="$ARTIFACTORY_URL/$SNAPSHOT_REPOSITORY/io/deepsense"

md5Value="`md5sum "${ARTIFACT_NAME}"`"
md5Value="${md5Value:0:32}"
sha1Value="`sha1sum "${ARTIFACT_NAME}"`"
sha1Value="${sha1Value:0:40}"

URL_WITH_UUID="${REPOSITORY_URL}/docker-compose/${UUID}/${ARTIFACT_NAME}"

echo "** INFO: Uploading $ARTIFACT_NAME to ${URL_WITH_UUID} **"
curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
 -H "X-Checksum-Md5: $md5Value" \
 -H "X-Checksum-Sha1: $sha1Value" \
 -T "${ARTIFACT_NAME}" \
 "${URL_WITH_UUID}"

URL_LATEST="${REPOSITORY_URL}/docker-compose/latest/${ARTIFACT_NAME}"

echo "** INFO: Uploading $ARTIFACT_NAME to ${URL_LATEST} **"
curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
 -H "X-Checksum-Md5: $md5Value" \
 -H "X-Checksum-Sha1: $sha1Value" \
 -T "${ARTIFACT_NAME}" \
 "${URL_LATEST}"
