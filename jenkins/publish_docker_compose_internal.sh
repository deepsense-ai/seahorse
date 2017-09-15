#!/bin/bash
# Copyright (c) 2016, CodiLime Inc.
#
# Releases docker-compose-internal.yml
#
# Usage: `jenkins/publish_docker_compose_internal.sh SEAHORSE_BUILD_TAG`

set -ex

cd `dirname $0`"/../"

# TODO Get rid of SEAHORSE_BUILD_TAG or make it optional

SEAHORSE_BUILD_TAG=$1

FRONTEND_TAG=${SEAHORSE_BUILD_TAG}
BACKEND_TAG=`git rev-parse HEAD`

echo "Generating docker compose file with docker images tagged with $SEAHORSE_BUILD_TAG"

ARTIFACT_NAME="docker-compose-internal.yml"

deployment/docker-compose/docker-compose.py --generate-only --yaml-file $ARTIFACT_NAME -f $FRONTEND_TAG -b $BACKEND_TAG

echo "Sending $ARTIFACT_NAME to snapshot artifactory"
source jenkins/publish_to_artifactory_function.sh
publish_to_artifactory $ARTIFACT_NAME seahorse-distribution/io/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}
