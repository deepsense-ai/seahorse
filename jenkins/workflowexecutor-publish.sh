#!/bin/bash -ex

# Copyright (c) 2016, CodiLime Inc.
#
# Builds workflowexecutor.jar and other jars (commons, deeplang) using sbt
# Publishes other jars to artifactory using sbt
# Publishes workflowexecutor jar to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and version in version.sbt
#
# Example usage from jenkins:
# ./jenkins/workflowexecutor-publish.sh


# Set working directory to project root file
# `dirname $0` gives folder containing script
cd `dirname $0`"/../"

echo "Starting sbt build"
SBT_OPTS="-XX:MaxPermSize=4G" \
  sbt -Dsbt.log.noformat=true clean test "+ sPublish" "+ workflowexecutor/assembly"

BASE_VERSION=`cat version.sbt | cut -d'=' -f 2 | xargs`
SNAPSHOT_REPOSITORY="seahorse-workflowexecutor-snapshot"
RELEASE_REPOSITORY="seahorse-workflowexecutor-release"
SEAHORSE_WORKFLOWEXECUTOR_REPOSITORY="seahorse-workflowexecutor"


# Functions useful for publishing artifacts
source jenkins/publish_to_artifactory_function.sh

function calculate_is_snapshot_version() {
  if [[ "$BASE_VERSION" == *SNAPSHOT ]]
  then
    export IS_SNAPSHOT=true
  else
    export IS_SNAPSHOT=false
  fi
}

function calculate_full_version() {
  echo "** Calculating version **"
  if [[ $IS_SNAPSHOT == true ]]
  then
    DATE=`date -u +%Y-%m-%d_%H-%M-%S`
    GIT_SHA=`git rev-parse HEAD`
    GIT_SHA_PREFIX=${GIT_SHA:0:7}
    export FULL_VERSION="${BASE_VERSION}-${DATE}-${GIT_SHA_PREFIX}"
  else
    export FULL_VERSION=${BASE_VERSION}
  fi
}

function publish_custom() {
  component=$1
  scalaVersion=$2
  artifactVersion=$3
  artifactRemoteName="${component}_${scalaVersion}-${artifactVersion}.jar"

  publish_to_artifactory "${component}/target/scala-${scalaVersion}/${component}.jar" "${component}_${scalaVersion}/${artifactVersion}/${artifactRemoteName}"
}

function publish_latest() {
  publish_custom $1 $2 "latest"
}

function publish_version() {
  component=$1
  artifactVersion=$2
  scalaVersion=$3
  artifactRemoteName="${component}_${scalaVersion}-${artifactVersion}.jar"

  if [[ $IS_SNAPSHOT == true ]]
  then
    url="${component}_${scalaVersion}/$BASE_VERSION/${artifactVersion}/${artifactRemoteName}"
  else
    url="${component}_${scalaVersion}/${artifactVersion}/${artifactRemoteName}"
  fi
  publish_to_artifactory "${component}/target/scala-${scalaVersion}/${component}.jar" $url
}

function publish_scala() {
  # snapshot, snapshot-latest, latest for Scala 2.11
  publish_version $1 $2 2.11
  publish_version $1 $3 2.11
  publish_latest $1 2.11
}

function publish_component() {
  if [ "${CUSTOM_TAG}" == "" ] ; then
    publish_scala $1 "${FULL_VERSION}" "${BASE_VERSION}-latest"
  else
    publish_custom $1 2.11 "${CUSTOM_TAG}"
  fi
}


# Publish projects jars
calculate_is_snapshot_version
calculate_full_version
publish_component workflowexecutor


# Publish WE uber-jar
if [ "$SEAHORSE_BUILD_TAG" != "" ]; then
  scalaVersions=("2.11")
  for scalaVersion in "${scalaVersions[@]}"
  do
    publish_to_artifactory "workflowexecutor/target/scala-${scalaVersion}/workflowexecutor.jar" \
      "${SEAHORSE_WORKFLOWEXECUTOR_REPOSITORY}/io/deepsense/deepsense-seahorse-workflowexecutor_${scalaVersion}/${SEAHORSE_BUILD_TAG}/workflowexecutor.jar"
  done
fi
