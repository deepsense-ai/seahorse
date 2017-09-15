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

# Releases docker-compose-internal.yml
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
publish_to_artifactory $ARTIFACT_NAME seahorse-distribution/ai/deepsense/${SEAHORSE_BUILD_TAG}/dockercompose/${ARTIFACT_NAME}
