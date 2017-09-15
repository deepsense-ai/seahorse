#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Prepares backend zips and publishes them to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and BASE_VERSION variable

BASE_VERSION="0.2.0"

function calculate_full_version() {
  echo "** Calculating version **"
  DATE=`date -u +%Y-%m-%d_%H-%M-%S`
  GIT_SHA=`git rev-parse HEAD`
  GIT_SHA_PREFIX=${GIT_SHA:0:7}
  export FULL_VERSION="${BASE_VERSION}-${DATE}-${GIT_SHA_PREFIX}-SNAPSHOT"
}

function publish() {
  component=$1
  artifactLocalName="${component}/target/universal/deepsense-${component}-${BASE_VERSION}-SNAPSHOT.zip"
  artifactVersion=$2
  artifactRemoteName="deepsense-${component}-${artifactVersion}.zip"

  echo "** INFO: Uploading $artifactLocalName to $ARTIFACTORY_URL/deepsense-${component}/${artifactVersion} **"
  md5Value="`md5sum "${artifactLocalName}"`"
  md5Value="${md5Value:0:32}"
  sha1Value="`sha1sum "${artifactLocalName}"`"
  sha1Value="${sha1Value:0:40}"

  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "${artifactLocalName}" \
   "${ARTIFACTORY_URL}/deepsense-${component}/${artifactVersion}/${artifactRemoteName}"
}

function publish_component() {
  publish $1 "${FULL_VERSION}"
  publish $1 "latest"
}

calculate_full_version
for component in entitystorage workflowmanager graphexecutor deploy-model-service
do
  publish_component ${component}
done
