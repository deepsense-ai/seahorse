#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Prepares backend zips and publishes them to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and BASE_VERSION variable

BASE_VERSION=`cat version.sbt | cut -d'=' -f 2 | xargs`
SNAPSHOT_REPOSITORY="deepsense-backend-snapshot"
RELEASE_REPOSITORY="deepsense-backend-release"

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
  artifactLocalName="${component}/target/universal/deepsense-${component}-${BASE_VERSION}.zip"
  artifactVersion=$2
  url=$3
  artifactRemoteName="deepsense-${component}-${artifactVersion}.zip"

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

function publishLatest() {
  component=$1
  artifactVersion="latest"
  artifactRemoteName="deepsense-${component}-${artifactVersion}.zip"

  publish $component $artifactVersion "${REPOSITORY_URL}/deepsense-${component}/deepsense-${component}-${artifactVersion}/${artifactRemoteName}"
}

function publishVersion() {
  component=$1
  artifactVersion=$2
  artifactRemoteName="deepsense-${component}-${artifactVersion}.zip"

  if [[ $IS_SNAPSHOT == true ]]
  then
    url="${REPOSITORY_URL}/deepsense-${component}/$BASE_VERSION/${artifactVersion}/${artifactRemoteName}"
  else
    url="${REPOSITORY_URL}/deepsense-${component}/${artifactVersion}/${artifactRemoteName}"
  fi
  publish $component $artifactVersion $url
}

function publish_component() {
  publishVersion $1 "${FULL_VERSION}"
  publishLatest $1
}

calculate_is_snapshot_version
calculate_repository_url
calculate_full_version
echo "FULL_VERSION="$FULL_VERSION > env.properties
for component in sessionmanager workflowmanager
do
  publish_component ${component}
done
