#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.
#

set -ex

cd `dirname $0`"/../"

BACKEND_TAG=`git rev-parse HEAD`

if [ -z ${SEAHORSE_BUILD_TAG+x} ]; then # SEAHORSE_BUILD_TAG is not defined
  FRONTEND_TAG="master-latest"
  export SEAHORSE_BUILD_TAG=$BACKEND_TAG
else # SEAHORSE_BUILD_TAG is defined
  FRONTEND_TAG=$SEAHORSE_BUILD_TAG
fi

./jenkins/scripts/sync_up_docker_images_with_git_repo.sh

SPARK_STANDALONE_MANAGEMENT="./seahorse-workflow-executor/docker/spark-standalone-cluster-manage.sh"
MESOS_SPARK_DOCKER_COMPOSE="testing/mesos-spark-cluster/mesos-cluster.dc.yml"

## Make sure that when job is aborted/killed all dockers will be turned off
function cleanup {
    $SPARK_STANDALONE_MANAGEMENT down
    docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE down
    (cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG logs > docker-compose.log)
    (cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG down)
}
trap cleanup EXIT

cleanup # in case something was already running

## Start Seahorse dockers

(cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG pull)
(cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG up -d)

## Start Spark Standalone cluster dockers

seahorse-workflow-executor/docker/spark-standalone-cluster/build-cluster-node-docker.sh
$SPARK_STANDALONE_MANAGEMENT up

## Start Mesos Spark cluster dockers

testing/mesos-spark-cluster/build-cluster-node-docker.sh
docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE up -d

export MESOS_MASTER_IP=10.254.0.2

## Run sbt tests

sbt e2etests/clean e2etests/test

SBT_EXIT_CODE=$?
exit $SBT_EXIT_CODE
