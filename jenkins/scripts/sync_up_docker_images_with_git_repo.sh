#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

# This script is idempotent and lazy. You can safely call it from other scripts to ensure dockers are built.
# TODO Before updating $BRANCH-latest, we could fetch and check if it is up-to date with remote
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

# Without it SM dockers SbtGit.GitKeys.gitCurrentBranch will resolve to git hash instead of branch
# and fail with $GIT_SHA-latest doesnt exists.
# TODO Fix SM docker building process and get rid of SEAHORSE_BUILD_TAG here
export SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG:-$GIT_SHA}" # SET if SEAHORSE_BUILD_TAG not set

function pullOrBuild {
  DOCKER_IMAGE=$1

  set +e
  docker pull docker-repo.deepsense.codilime.com/deepsense_io/$DOCKER_IMAGE:$GIT_SHA
  IMAGE_EXISTS_IF_ZERO=$?
  set -e
  if [ "$IMAGE_EXISTS_IF_ZERO" -gt 0 ]; then
    ./jenkins/manage-docker.py --build --images $DOCKER_IMAGE
  else
    echo "Docker image $DOCKER_IMAGE:$GIT_SHA already exists"
  fi

  ./jenkins/manage-docker.py --publish --images $DOCKER_IMAGE
}

pullOrBuild "deepsense-proxy"
pullOrBuild "deepsense-rabbitmq"
pullOrBuild "deepsense-h2"
pullOrBuild "deepsense-spark"
pullOrBuild "deepsense-mesos-spark"
pullOrBuild "deepsense-sessionmanager"
pullOrBuild "deepsense-workflowmanager"
pullOrBuild "deepsense-datasourcemanager"
pullOrBuild "deepsense-schedulingmanager"
pullOrBuild "deepsense-libraryservice"
pullOrBuild "deepsense-notebooks"
pullOrBuild "deepsense-authorization"
pullOrBuild "deepsense-mail"
