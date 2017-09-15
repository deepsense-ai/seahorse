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


function calculate_is_snapshot_version() {
  if [[ "$BASE_VERSION" == *SNAPSHOT ]]
  then
    export IS_SNAPSHOT=true
  else
    export IS_SNAPSHOT=false
  fi
}

function calculate_repository_url() {
  if [[ $IS_SNAPSHOT == true ]]
  then
    export REPOSITORY_URL="$ARTIFACTORY_URL/$SNAPSHOT_REPOSITORY/io/deepsense"
  else
    export REPOSITORY_URL="$ARTIFACTORY_URL/$RELEASE_REPOSITORY/io/deepsense"
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

function publish() {
  component=$1
  artifactVersion=$2
  # artifactLocalName should be like "${component}/target/scala-${scalaVersion}/${component}.jar"
  artifactLocalName=$3
  url=$4


  echo "** INFO: Uploading $artifactLocalName to ${url} **"
  md5Value="`md5sum "${artifactLocalName}"`"
  md5Value="${md5Value:0:32}"
  sha1Value="`sha1sum "${artifactLocalName}"`"
  sha1Value="${sha1Value:0:40}"

  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "${artifactLocalName}" \
   "${url}"
}

function publish_custom() {
  component=$1
  scalaVersion=$2
  artifactVersion=$3
  artifactRemoteName="${component}_${scalaVersion}-${artifactVersion}.jar"

  publish $component $artifactVersion "${component}/target/scala-${scalaVersion}/${component}.jar" "${REPOSITORY_URL}/${component}_${scalaVersion}/${artifactVersion}/${artifactRemoteName}"
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
    url="${REPOSITORY_URL}/${component}_${scalaVersion}/$BASE_VERSION/${artifactVersion}/${artifactRemoteName}"
  else
    url="${REPOSITORY_URL}/${component}_${scalaVersion}/${artifactVersion}/${artifactRemoteName}"
  fi
  publish $component $artifactVersion "${component}/target/scala-${scalaVersion}/${component}.jar" $url
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

function prepare_system_tests_propfile() {

  PROPFILE_NAME=latestpropfile

  if [ $GIT_BRANCH != "origin/master" ]
  then
      PROPFILE_NAME=productionpropfile
  fi

  SYSTEM_TESTS_VERSION=latest
  WORKFLOW_EXECUTOR_SOURCE=seahorse-workflowexecutor-snapshot
  WORKFLOW_EXECUTOR_VERSION=${BASE_VERSION}-latest

  if [ -f latestpropfile ];
  then
      rm latestpropfile
  fi

  if [ -f productionpropfile ];
  then
      rm productionpropfile
  fi

  echo SYSTEM_TESTS_VERSION=$(echo $SYSTEM_TESTS_VERSION) >> $PROPFILE_NAME
  echo WORKFLOW_EXECUTOR_SOURCE=$(echo $WORKFLOW_EXECUTOR_SOURCE) >> $PROPFILE_NAME
  echo WORKFLOW_EXECUTOR_VERSION=$(echo $WORKFLOW_EXECUTOR_VERSION) >> $PROPFILE_NAME
}


calculate_is_snapshot_version
calculate_full_version
calculate_repository_url
publish_component workflowexecutor
prepare_system_tests_propfile

if [ "$SEAHORSE_BUILD_TAG" != "" ]; then
  scalaVersions=("2.11")
  for scalaVersion in "${scalaVersions[@]}"
  do
    publish "workflowexecutor" $SEAHORSE_BUILD_TAG "workflowexecutor/target/scala-${scalaVersion}/workflowexecutor.jar" \
      "${ARTIFACTORY_URL}/${SEAHORSE_WORKFLOWEXECUTOR_REPOSITORY}/io/deepsense/deepsense-seahorse-workflowexecutor_${scalaVersion}/${SEAHORSE_BUILD_TAG}/workflowexecutor.jar"
  done

fi
