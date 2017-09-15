#!/bin/bash -x
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


# usage: clean-docker.sh [--all]

DOCKER_COMPOSE_PREFIX=migratione2etests

if [[ $1 == "--all" ]]; then
    echo "Removing all processess.."
    docker ps -a -q --filter name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker rm -f -v
else
    echo "Removing exited processes..."
    docker ps -q --filter status=exited,name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker rm -v
fi

echo "Removing unused networks..."
docker network ls -q --filter type=custom,name="$DOCKER_COMPOSE_PREFIX" | xargs -r docker network rm 2> /dev/null
