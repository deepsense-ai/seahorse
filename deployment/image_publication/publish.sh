#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.
#
# Usage: ./publish.sh SEAHORSE_BUILD_TAG API_VERSION

SEAHORSE_BUILD_TAG=$1
API_VERSION=$2
echo "SEAHORSE_BUILD_TAG='$SEAHORSE_BUILD_TAG'; API_VERSION='$API_VERSION'"
REPOSITORY="seahorse-distribution"
BOX_FILE_LOCAL_NAME="seahorse-vm.box"
BOX_FILE_REMOTE_NAME="seahorse-vm-${API_VERSION}.box"

ARTIFACTORY_CREDENTIALS=$HOME/.artifactory_credentials
ARTIFACTORY_USER=`grep "user=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_PASSWORD=`grep "password=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
ARTIFACTORY_URL=`grep "host=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`

#Publishes file (first parameter) with given version (second parameter)
function publish() {
  artifactLocalName=$1
  artifactVersion=$2
  url=$3

  echo "** INFO: Uploading $artifactLocalName to $url **"
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

function publishVersion() {
  artifactLocalName=$1
  artifactRemoteName=$2
  artifactVersion=$3

  url="$ARTIFACTORY_URL/$REPOSITORY/io/deepsense/${artifactVersion}/vagrant/${artifactRemoteName}"

  publish $artifactLocalName $artifactVersion $url
}

# Publish vagrant box file
publishVersion "${BOX_FILE_LOCAL_NAME}" "${BOX_FILE_REMOTE_NAME}" "${SEAHORSE_BUILD_TAG}"

# Publish Vagrantfile
URL="$ARTIFACTORY_URL/$REPOSITORY/io/deepsense/${SEAHORSE_BUILD_TAG}/vagrant/${BOX_FILE_REMOTE_NAME}"
sed -e "s#SEAHORSE_BOX_URL_VARIABLE#${URL}#" -e "s#SEAHORSE_BOX_NAME_VARIABLE#seahorse-vm-${API_VERSION}#" -e "s#SEAHORSE_BOX_HOSTNAME_VARIABLE#seahorse-vm-${API_VERSION//./-}#" Vagrantfile.template > Vagrantfile
publishVersion "Vagrantfile" "Vagrantfile-internal" "${SEAHORSE_BUILD_TAG}"
