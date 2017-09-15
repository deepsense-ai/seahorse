#!/bin/bash

# Copyright (c) 2015, CodiLime, Inc.
#
# Prepares frontend zip and publishes it to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and BASE_VERSION variable

BASE_VERSION="0.2.0-frontend"
NPM_REGISTRY_URL="http://10.10.1.124:4873"

function prepare_environment() {
  echo "** Preparing Environment **"
  sudo npm install -g gulp
  sudo npm install -g npmrc
  npmrc -c codilime
  npmrc codilime
  npm set registry $NPM_REGISTRY_URL
  npm install
}

function build() {
   echo "** Building package **"
   gulp clean
   gulp build
}

function calculate_full_version() {
   echo "** Calculating version **"
   DATE=`date -u +%Y-%m-%d_%H-%M-%S`
   GIT_SHA=`git rev-parse HEAD`
   GIT_SHA_PREFIX=${GIT_SHA:0:7}
   export FULL_VERSION="${BASE_VERSION}-${DATE}-${GIT_SHA_PREFIX}-SNAPSHOT"
}

function package() {
   echo "** Preparing zip package **"
   zip -r -q "${FULL_VERSION}.zip" build
}

#Publishes zip file (first parameter) with given version (second parameter)
function publish() {
  artifactLocalName=$1
  artifactVersion=$2
  artifactRemoteName="${artifactVersion}.zip"

  echo "** INFO: Uploading $artifactLocalName to $ARTIFACTORY_URL/${artifactVersion} **"
  md5Value="`md5sum "${artifactLocalName}"`"
  md5Value="${md5Value:0:32}"
  sha1Value="`sha1sum "${artifactLocalName}"`"
  sha1Value="${sha1Value:0:40}"

  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "${artifactLocalName}" \
   "${ARTIFACTORY_URL}/${artifactVersion}/${artifactRemoteName}"
}

function clean() {
  echo "** Removing zip file **"
  rm ${FULL_VERSION}.zip
}

prepare_environment
build
calculate_full_version
package
publish "${FULL_VERSION}.zip" "${FULL_VERSION}"
publish "${FULL_VERSION}.zip" "frontend-latest"
clean
