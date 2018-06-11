#!/usr/bin/env bash
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
#

if [[ "$#" -eq 0 ]]; then
  echo "error: no tests specified"
  echo "supported options:"
  echo "-b, --batch"
  echo "-s, --session"
  echo "-a, --all"
  echo "--cleanup-only"
  exit 1
fi

# parse parameters
RUN_BATCH_TESTS=false
RUN_SESSION_TESTS=false
CLEANUP_ONLY=false
while (( "$#" )); do
  key="$1"

  case $key in
    -b|--batch)
    RUN_BATCH_TESTS=true
    shift
    ;;
    -s|--session)
    RUN_SESSION_TESTS=true
    shift
    ;;
    -a|--all)
    RUN_BATCH_TESTS=true
    RUN_SESSION_TESTS=true
    ;;
    --cleanup-only)
    CLEANUP_ONLY=true
    shift
    ;;
    *)
    echo "error: unknown option $key"
    exit 1
    ;;
  esac
  shift
done

set -ex
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

RUN_ID="seahorse-e2e-tests"
export CLUSTER_ID=$RUN_ID # needed by spark-standalone-cluster-manage.sh
export NETWORK_NAME="sbt-test-$RUN_ID" # needed by spark-standalone-cluster-manage.sh

export GIT_TAG=`git rev-parse HEAD`

SPARK_STANDALONE_MANAGEMENT="./seahorse-workflow-executor/docker/spark-standalone-cluster-manage.sh"
MESOS_SPARK_DOCKER_COMPOSE="testing/mesos-spark-cluster/mesos-cluster.dc.yml"
YARN_SPARK_DOCKER_COMPOSE="testing/yarn-spark-cluster/yarn-cluster.dc.yml"

SPARK_VERSION="2.1.1"
HADOOP_VERSION="2.7"

## Make sure that when job is aborted/killed all dockers will be turned off
function cleanup {
  $SPARK_STANDALONE_MANAGEMENT down $SPARK_VERSION
  docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE down
  docker-compose -f $YARN_SPARK_DOCKER_COMPOSE down
  deployment/docker-compose/docker-compose.py -f $GIT_TAG -b $GIT_TAG -p $RUN_ID logs > docker-compose.log
  deployment/docker-compose/docker-compose.py -f $GIT_TAG -b $GIT_TAG -p $RUN_ID down
}

cleanup
if $CLEANUP_ONLY ; then
  exit 0
fi

trap cleanup EXIT

./build/manage-docker.py -b --all

## Start Seahorse dockers
(
 ./build/prepare_sdk_dependencies.sh
 cd seahorse-sdk-example
 sbt clean assembly
 cd ../deployment/docker-compose

 # Create volumen before running docker so it doesnt have root priviledges
 # https://github.com/docker/docker/issues/2259
 mkdir -p data
 mkdir -p jars
 cp -r ../../seahorse-sdk-example/target/scala-2.11/*.jar jars

 ./docker-compose.py -f $GIT_TAG -b $GIT_TAG --generate-only --yaml-file docker-compose.yml
 ./docker-compose.py -f $GIT_TAG -b $GIT_TAG -p $RUN_ID up -d
)

### TODO Revive standalone tests
## Start Spark Standalone cluster dockers
#$SPARK_STANDALONE_MANAGEMENT up $SPARK_VERSION

## Get and export Spark Standalone cluster IP
#INSPECT_FORMAT="{{(index (index .NetworkSettings.Networks \"$NETWORK_NAME\").IPAddress )}}"
#export SPARK_STANDALONE_MASTER_IP=$(docker inspect --format "$INSPECT_FORMAT" sparkMaster-$RUN_ID)

### TODO Revive mesos tests
## Start Mesos Spark cluster dockers
#testing/mesos-spark-cluster/build-cluster-node-docker.sh $SPARK_VERSION $HADOOP_VERSION
#docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE up -d

export MESOS_MASTER_IP=10.254.0.2

## Start YARN Spark cluster dockers
HADOOP_CONF_DIR="deployment/docker-compose/data/hadoop"
mkdir -p $HADOOP_CONF_DIR
cp testing/yarn-spark-cluster/cluster-node-docker/generic-hadoop-node/hadoop-conf/* $HADOOP_CONF_DIR

testing/yarn-spark-cluster/build-cluster-node-docker.sh
docker-compose -f $YARN_SPARK_DOCKER_COMPOSE up -d

export YARN_MASTER_IP=10.254.1.2

## Run sbt tests
TESTS_TO_RUN=""
if [ "${RUN_SESSION_TESTS}" = true ]; then
  TESTS_TO_RUN="${TESTS_TO_RUN} ai.deepsense.e2etests.session.*"
fi
if [ "${RUN_BATCH_TESTS}" = true ]; then
  TESTS_TO_RUN="${TESTS_TO_RUN} ai.deepsense.e2etests.batch.*"
fi
sbt "e2etests/clean" "e2etests/test:testOnly ${TESTS_TO_RUN}"

