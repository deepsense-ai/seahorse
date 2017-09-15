#!/bin/bash
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

# Usage: `build/publish_docker_compose_internal.sh GIT_TAG`

set -ex

cd `dirname $0`"/../"


GIT_TAG=$1

echo "Generating docker compose file with docker images tagged with $GIT_TAG"

ARTIFACT_NAME="docker-compose.yml"
deployment/docker-compose/docker-compose.py --generate-only --yaml-file $ARTIFACT_NAME -f $GIT_TAG -b $GIT_TAG
