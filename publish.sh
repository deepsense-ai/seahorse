#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Prepares frontend zip and publishes it to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and BASE_VERSION variable

function jsonValue() {
  KEY=$1
  num=$2
  awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}

BASE_VERSION=`cat package.json | jsonValue version 1 | xargs`
SNAPSHOT_REPOSITORY="deepsense-frontend-snapshot"
RELEASE_REPOSITORY="deepsense-frontend-release"
NPM_REGISTRY_URL="http://10.10.1.124:4873"

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
    export REPOSITORY_URL="$ARTIFACTORY_URL/$SNAPSHOT_REPOSITORY/io/deepsense/deepsense-frontend"
  else
    export REPOSITORY_URL="$ARTIFACTORY_URL/$RELEASE_REPOSITORY/io/deepsense/deepsense-frontend"
  fi
}

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

function package() {
  echo "** Preparing zip package **"
  zip -r -q "${FULL_VERSION}.zip" build
}

#Publishes zip file (first parameter) with given version (second parameter)
function publish() {
  artifactLocalName=$1
  artifactVersion=$2
  url=$3

  echo "** INFO: Uploading $artifactLocalName to $ARTIFACTORY_URL/${artifactVersion} **"
  md5Value="`md5sum "${artifactLocalName}"`"
  md5Value="${md5Value:0:32}"
  sha1Value="`sha1sum "${artifactLocalName}"`"
  sha1Value="${sha1Value:0:40}"

  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "${artifactLocalName}" \
   "$url"
}

function publishLatest() {
  artifactLocalName=$1
  artifactVersion="frontend-latest"
  artifactRemoteName="${artifactVersion}.zip"

  publish $artifactLocalName $artifactVersion "${REPOSITORY_URL}/${artifactVersion}/${artifactRemoteName}"
}

function publishVersion() {
  artifactLocalName=$1
  artifactVersion=$2
  artifactRemoteName="${artifactVersion}.zip"

  if [[ $IS_SNAPSHOT == true ]]
  then
    url="${REPOSITORY_URL}/$BASE_VERSION/${artifactVersion}/${artifactRemoteName}"
  else
    url="${REPOSITORY_URL}/${artifactVersion}/${artifactRemoteName}"
  fi
  publish $artifactLocalName $artifactVersion $url
}

function clean() {
  echo "** Removing zip file **"
  rm ${FULL_VERSION}.zip
}

prepare_environment
build
calculate_is_snapshot_version
calculate_full_version
calculate_repository_url
package
publishVersion "${FULL_VERSION}.zip" "${FULL_VERSION}"
publishLatest "${FULL_VERSION}.zip"
clean
