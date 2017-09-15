#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.


VERSION=$1
if [ -z "$VERSION" ]
then
    VERSION=$(date +"%Y-%m-%d_%H-%M-%S")
    REPOSITORY="seahorse-bundled-image"
else
    echo "VERSION='$VERSION'"
    REPOSITORY="seahorse-bundled-image-release"
fi

BOX_NAME="seahorse-vm.box"

#Publishes file (first parameter) with given version (second parameter)
function publish() {
  artifactLocalName=$1
  artifactVersion=$2
  url=$3

  echo "** INFO: Uploading $artifactLocalName to $ARTIFACTORY_URL/$REPOSITORY/${artifactVersion} **"
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
  artifactVersion=$2

  url="$ARTIFACTORY_URL/$REPOSITORY/${artifactVersion}/${artifactLocalName}"

  publish $artifactLocalName $artifactVersion $url
}

publishVersion "${BOX_NAME}" "${VERSION}"

URL="$ARTIFACTORY_URL/$REPOSITORY/${VERSION}/${BOX_NAME}"
sed "s#seahorsevm.vm.box_url = \"\"#seahorsevm.vm.box_url = \"${URL}\"#" Vagrantfile.template > Vagrantfile

publishVersion "Vagrantfile" "${VERSION}"
