#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Releases docker-compose-internal.yml
#
# Usage: `jenkins/publish_docker_compose_internal.sh SEAHORSE_BUILD_TAG` from deepsense-backend catalog

set -e

DEEPSENSE_REGISTRY="docker-repo.deepsense.codilime.com/deepsense_io"
SEAHORSE_BUILD_TAG=$1

echo "This script assumes it is run from deepsense-backend directory"

echo "Generating docker compose file with docker images tagged with $SEAHORSE_BUILD_TAG"

ARTIFACT_NAME_TMPL="docker-compose-internal.yml.tmpl"
ARTIFACT_NAME="docker-compose-internal.yml"

DOCKER_COMPOSE_TMPL="deployment/docker-compose/docker-compose.tmpl.yml"
rm -f $ARTIFACT_NAME_TMPL
rm -f $ARTIFACT_NAME
sed 's|\$DOCKER_REPOSITORY|'"$DEEPSENSE_REGISTRY"'|g ; s|\$DOCKER_TAG|'"$SEAHORSE_BUILD_TAG"'|g' $DOCKER_COMPOSE_TMPL >> $ARTIFACT_NAME_TMPL
deployment/docker-compose/prepare_docker-compose $ARTIFACT_NAME_TMPL $ARTIFACT_NAME $SEAHORSE_BUILD_TAG

echo 'Sending $ARTIFACT_NAME to snapshot artifactory'

ARTIFACTORY_CREDENTIALS=$HOME/.artifactory_credentials

ARTIFACTORY_USER=`grep "user=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_PASSWORD=`grep "password=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_URL=`grep "host=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`

REPOSITORY_URL="$ARTIFACTORY_URL/seahorse-distribution/io/deepsense"

echo "Sending $ARTIFACT_NAME"

md5Value="`md5sum "${ARTIFACT_NAME}"`"
md5Value="${md5Value:0:32}"
sha1Value="`sha1sum "${ARTIFACT_NAME}"`"
sha1Value="${sha1Value:0:40}"

URL_WITH_TAG="${REPOSITORY_URL}/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}"

echo "** INFO: Uploading $ARTIFACT_NAME to ${URL_WITH_TAG} **"
curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
 -H "X-Checksum-Md5: $md5Value" \
 -H "X-Checksum-Sha1: $sha1Value" \
 -T "${ARTIFACT_NAME}" \
 "${URL_WITH_TAG}"

