#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Releases docker-compose-internal.yml
#
# Usage: `jenkins/publish_docker_compose_internal.sh SEAHORSE_BUILD_TAG` from deepsense-backend catalog

set -e

./jenkins/scripts/checkout-submodules.sh

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
source jenkins/publish_to_artifactory_function.sh
publish_to_artifactory $ARTIFACT_NAME seahorse-distribution/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}
