#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

# This script is idempotent and lazy. You can safely call it from other scripts to ensure dockers are built.

cd `dirname $0`"/../../"

if [[ -n $(git status --porcelain) ]]; then
  set +x # for more readable output
  echo "####################################"
  echo "# Repository has unstaged files!"
  echo "# Docker images are function of git commits."
  echo "# Docker tags are git hashes."
  echo "# If you have unstaged changes your git hash is not affected and output docker image would be undeterministic"
  echo "# In order to to use this script commit all changes first."
  echo "# Aborting..."
  echo "####################################"
  exit 1
fi

GIT_SHA=`git rev-parse HEAD`

function pullOrBuild {
  DOCKER_IMAGE=$1
  BUILD_SCRIPT=$2

  set +e
  docker pull docker-repo.deepsense.codilime.com/deepsense_io/$DOCKER_IMAGE:$GIT_SHA
  IMAGE_EXISTS_IF_ZERO=$?
  set -e
  if [ "$IMAGE_EXISTS_IF_ZERO" -gt 0 ]; then
    $BUILD_SCRIPT
  else
    echo "Docker image $DOCKER_IMAGE:$GIT_SHA already exists"
  fi
}

pullOrBuild "deepsense-rabbitmq" "./jenkins/rabbitmq-publish.sh"
pullOrBuild "deepsense-h2" "./jenkins/h2-docker-publish.sh"
pullOrBuild "deepsense-spark" "./jenkins/publish_spark_docker.sh"
pullOrBuild "deepsense-mesos-spark" "./jenkins/publish_spark_docker_mesos.sh"
pullOrBuild "deepsense-sessionmanager" "./jenkins/sessionmanager-docker-publish.sh"
pullOrBuild "deepsense-workflowmanager" "./jenkins/workflowmanager-publish.sh"
pullOrBuild "deepsense-datasourcemanager" "./jenkins/datasourcemanager-publish.sh"
pullOrBuild "deepsense-schedulingmanager" "./jenkins/schedulingmanager-publish.sh"
pullOrBuild "deepsense-libraryservice" "./jenkins/libraryservice-publish.sh"
pullOrBuild "deepsense-notebooks" "./jenkins/notebooks-publish.sh"