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


SCRIPT_DIR=$(dirname $0)

. $SCRIPT_DIR/common.sh > /dev/null

export PROJECT_ROOT

function cleanup() {
    for dir in "${WORKDIR_NO_TIMESTAMP}"*; do
        if [[ -d $dir ]]; then
            pushd $dir > /dev/null
            docker-compose kill > /dev/null
            if [[ ! -f docker-compose.log ]]; then
                docker-compose logs > docker-compose.log
            fi
            docker-compose down -v > /dev/null
            popd > /dev/null
        fi
    done
    $SCRIPT_DIR/clean_docker.sh --all > /dev/null
}

cleanup

if [[ $CLEANUP_ONLY  ]]; then
    exit 0;
fi;

trap cleanup EXIT

$PROJECT_ROOT/build/manage-docker.py -b --all

WORKDIR=$($SCRIPT_DIR/prepare_env.sh -b $BACKEND_TAG -f $FRONTEND_TAG) || exit 1

pushd "$WORKDIR" > /dev/null

docker-compose up -d > /dev/null

pushd "$PROJECT_ROOT" > /dev/null

sbt "e2etests/clean" \
    "e2etests/test:testOnly ai.deepsense.e2etests.session.AllExampleWorkflowsWorkOnLocalClusterSessionTest" \
    "e2etests/test:testOnly ai.deepsense.e2etests.batch.AllExampleWorkflowsWorkOnLocalClusterBatchTest"

popd > /dev/null

popd > /dev/null
