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


#this script prepares the seahorse environment, specifically the
#database, containing 1.3 workflows to be migrated

{

SCRIPT_DIR=`dirname $0`

. "$SCRIPT_DIR/common.sh"

WORKDIR="$WORKDIR_NO_TIMESTAMP".`date +%Y%m%d%H%M%S`
mkdir -p $WORKDIR/h2-data
pushd $WORKDIR

WORKFLOWS_DIR="$PROJECT_ROOT/workflowmanager/src/test/resources/versionconverter"

DOCKER_COMPOSE_PY="$PROJECT_ROOT/deployment/docker-compose/docker-compose.py"
DOCKER_COMPOSE_CALL="$DOCKER_COMPOSE_PY -b $BACKEND_TAG -f $FRONTEND_TAG"

GENERATE_EXAMPLES_PY="$PROJECT_ROOT/deployment/generate_examples/generate_workflow_examples_sql.py"

WORKFLOWMANAGER_URL=http://127.0.0.1:33321/v1/workflows
H2_JAR=h2-1.4.193.jar
H2_JAR_URL=http://repo2.maven.org/maven2/com/h2database/h2/1.4.193/$H2_JAR
H2_JDBC_URL=jdbc:h2:"$WORKDIR/h2-data/workflowmanager;DATABASE_TO_UPPER=false"
H2_RUN_SCRIPT_CLASS=org.h2.tools.RunScript
H2_SHELL_CLASS=org.h2.tools.Shell

INSERT_TEST_WORKFLOWS_SCRIPT=insert_test_workflows.sql

function wait_for_workflowmanager() {
    until curl -m 1 $WORKFLOWMANAGER_URL; do sleep 1; done
}

function fetch_h2_jar() {
    curl -O $H2_JAR_URL
}

function create_sql_script() {
    python "$GENERATE_EXAMPLES_PY" "$WORKFLOWS_DIR" > "$INSERT_TEST_WORKFLOWS_SCRIPT"
}

fetch_h2_jar
#create db file, so that it's not owned by root
java -cp $H2_JAR $H2_SHELL_CLASS -url $H2_JDBC_URL <<<"quit"

$DOCKER_COMPOSE_CALL --generate-only
docker-compose up -d || exit 1
wait_for_workflowmanager

# db and its tables are created at this point

docker-compose kill
docker-compose down -v

create_sql_script # that inserts 1.3 worklows as examples
java -cp $H2_JAR $H2_RUN_SCRIPT_CLASS -url $H2_JDBC_URL -script ./$INSERT_TEST_WORKFLOWS_SCRIPT || exit 1

popd

} > /dev/null

echo $WORKDIR
