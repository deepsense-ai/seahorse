#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Prepares frontend zip and publishes it to artifactory.
# This script expects no external parameters.
# Version is calculated from current git sha, current time and BASE_VERSION variable

set -e

function jsonValue() {
  KEY=$1
  num=$2
  awk -F"[,:}]" '{for(i=1;i<=NF;i++){if($i~/'$KEY'\042/){print $(i+1)}}}' | tr -d '"' | sed -n ${num}p
}

CUSTOM_TAG="$1"
BASE_VERSION=`cat package.json | jsonValue version 1 | xargs`
SNAPSHOT_REPOSITORY="deepsense-frontend-snapshot"
RELEASE_REPOSITORY="deepsense-frontend-release"
NPM_REGISTRY_URL="https://registry.npmjs.org"

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
  # this returns an error unless it's run for the first time on a machine
  # we know about it and want to continue anyway
  npmrc -c codilime || test 1
  npmrc codilime
  npm set registry $NPM_REGISTRY_URL

  sudo npm install -g webpack
  sudo npm install -g npmrc
  npm install
}

function build() {
  echo "** Building package **"
  npm run dist
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

function add_build_info_file() {
  GIT_SHA=`git rev-parse HEAD`
  BUILD_DATE=`date`
  RESULT_FILE="dist/build-info.txt"
  echo "API VERSION: "$FULL_VERSION > $RESULT_FILE
  echo "BUILD DATE: "$BUILD_DATE >> $RESULT_FILE
  echo "GIT SHA: "$GIT_SHA >> $RESULT_FILE
}

function add_version_file() {
  echo $FULL_VERSION > dist/FULL_VERSION
}

function package() {
  echo "** Preparing zip package **"
  zip -r -q "${FULL_VERSION}.zip" dist
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

function publish_custom() {
  artifactLocalName=$1
  artifactVersion="frontend-$2"
  artifactRemoteName="${artifactVersion}.zip"

  publish $artifactLocalName $artifactVersion "${REPOSITORY_URL}/${artifactVersion}/${artifactRemoteName}"
}

function publish_latest() {
  publish_custom $1 latest
}

function publish_version() {
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

function create_jenkins_env_file {
  echo "FULL_VERSION="$FULL_VERSION > env.properties
  echo "BRANCH_NAME="`echo $FULL_VERSION | cut -d'.' -f 1,2` >> env.properties
}

function clean() {
  echo "** Removing zip file **"
  rm ${FULL_VERSION}.zip
}

./build.sh
calculate_is_snapshot_version
calculate_full_version
calculate_repository_url
add_build_info_file
add_version_file
create_jenkins_env_file
package

if [ "${CUSTOM_TAG}" == "" ] ; then
  publish_version "${FULL_VERSION}.zip" "${FULL_VERSION}"
  publish_latest "${FULL_VERSION}.zip"
else
  publish_custom "${FULL_VERSION}.zip" "${CUSTOM_TAG}"
fi

clean

set +e
trap times EXIT
