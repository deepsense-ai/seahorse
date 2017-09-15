#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.
#

set -ex

# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

if [ -z ${SEAHORSE_BUILD_TAG+x} ]; then # SEAHORSE_BUILD_TAG is not defined
  FRONTEND_TAG="master-latest"
  BACKEND_TAG=`git rev-parse HEAD`

  # SEAHORSE_BUILD_TAG is set for backward compability - some scripts require it
  export SEAHORSE_BUILD_TAG=$BACKEND_TAG
else # SEAHORSE_BUILD_TAG is defined
  FRONTEND_TAG=$SEAHORSE_BUILD_TAG
  BACKEND_TAG=$SEAHORSE_BUILD_TAG
fi

./jenkins/scripts/sync_up_docker_images_with_git_repo.sh

SPARK_STANDALONE_DOCKER_COMPOSE="testing/spark-standalone-cluster/standalone-cluster.dc.yml"
MESOS_SPARK_DOCKER_COMPOSE="testing/mesos-spark-cluster/mesos-cluster.dc.yml"

## Make sure that when job is aborted/killed all dockers will be turned off
function cleanup {
    docker-compose -f $SPARK_STANDALONE_DOCKER_COMPOSE down
    docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE down
    (cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG down)
}
trap cleanup EXIT

cleanup # in case something was already running

## Start Seahorse dockers

(cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG pull)
# destroy dockercompose_default, so we can recreate it with proper id
(cd deployment/docker-compose ; ./docker-compose $FRONTEND_TAG $BACKEND_TAG down)
(
 cd e2etestssdk
 sbt clean assembly
 cd ../deployment/docker-compose
 mkdir -p jars
 cp -r ../../e2etestssdk/target/scala-2.11/*.jar jars
 ./docker-compose $FRONTEND_TAG $BACKEND_TAG up -d
)

## Start Spark Standalone cluster dockers

testing/spark-standalone-cluster/build-cluster-node-docker.sh
docker-compose -f $SPARK_STANDALONE_DOCKER_COMPOSE up -d

export SPARK_STANDALONE_MASTER_IP=$(
docker inspect --format='{{.Name}}-{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' $(docker ps -q) \
  | grep sparkMaster \
  | cut -f2 -d"-"
)

## Start Mesos Spark cluster dockers

testing/mesos-spark-cluster/build-cluster-node-docker.sh
docker-compose -f $MESOS_SPARK_DOCKER_COMPOSE up -d

export MESOS_MASTER_IP=10.254.0.2

## Run sbt tests

sbt e2etests/clean e2etests/test

SBT_EXIT_CODE=$?
exit $SBT_EXIT_CODE
