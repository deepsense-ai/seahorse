#!/usr/bin/env bash

set -ex

SPARK_STANDALONE_DOCKER_COMPOSE="testing/spark-standalone-cluster/standalone-cluster.dc.yml"

## Make sure that when job is aborted/killed all dockers will be turned off
function cleanup {
    docker-compose -f $SPARK_STANDALONE_DOCKER_COMPOSE down
    (cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
}
trap cleanup EXIT

cleanup # in case something was already running

## Start Seahorse dockers

(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG pull)
# destroy dockercompose_default, so we can recreate it with proper id
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG down)
(cd deployment/docker-compose ; ./docker-compose $SEAHORSE_BUILD_TAG up -d)

## Start Spark Standalone cluster dockers

testing/spark-standalone-cluster/build-cluster-node-docker.sh
docker-compose -f $SPARK_STANDALONE_DOCKER_COMPOSE up -d

SPARK_STANDALONE_MASTER_IP=$(
docker inspect --format='{{.Name}}-{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q) \
  | grep sparkMaster \
  | cut -f2 -d"-"
)

## Run sbt tests

export SPARK_STANDALONE_MASTER_IP=$SPARK_STANDALONE_MASTER_IP
sbt e2etests/clean e2etests/test
SBT_EXIT_CODE=$?
exit $SBT_EXIT_CODE
