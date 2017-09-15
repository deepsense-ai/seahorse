#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

usage() {
    echo "Publishes System Tests Framework to proper Artifactory repository"
    echo "Usage: $0 ds_studio|workflow_executor ARTIFACT_VERSION [--draft]"
    exit 1
}

if [ -z "$1" ]; then
    usage
fi

if [ -z "$2" ]; then
    usage
fi

# Test target: ds_studio / workflow_executor
SYSTEM_TESTS_TARGET="$1"
# Version of system_tests artifact
ARTIFACT_VERSION="$2"
# system_tests artifact filename
ARTIFACT_FILE_PATH=system_tests.zip
# User with adding and overwriting permissions
# Url to Artifactory repository

if [ "$3" == "--draft" ]; then
else
fi


# Zip system_tests files into single archive
# NOTE: system_tests-$ARTIFACT_VERSION.zip is automatically removed from set of files to include in archive
if [ "$1" == "workflow_executor" ]; then
    find . -not -path "./ds_studio/*" -not -name "ds_studio" -not -path "./test-output/*"\
        -not -name "test-output" -not -name "publish.sh" -not -name "*.zip" -not -name "\.*"\
        -not -name "*.pyc" -not -name "*.html" -not -name "*.xml"\
        -not -name "workflowexecutor*.jar"\
        | zip -@ $ARTIFACT_FILE_PATH
elif [ "$1" == "ds_studio" ]; then
    find . -not -path "./workflow_executor/*" -not -name "workflow_executor" -not -path "./test-output/*"\
        -not -name "test-output" -not -name "publish.sh" -not -name "*.zip" -not -name "\.*"\
        -not -name "*.pyc" -not -name "*.html" -not -name "*.xml"\
        -not -name "workflowexecutor*.jar"\
        | zip -@ $ARTIFACT_FILE_PATH
else
    usage
fi

if [ ! -f "$ARTIFACT_FILE_PATH" ]; then
    echo "ERROR: Could not prepare zip archive!"
    exit 1
fi

which md5sum || exit $?
which sha1sum || exit $?

md5Value="`md5sum "$ARTIFACT_FILE_PATH"`"
md5Value="${md5Value:0:32}"
sha1Value="`sha1sum "$ARTIFACT_FILE_PATH"`"
sha1Value="${sha1Value:0:40}"
fileName="`basename "$ARTIFACT_FILE_PATH"`"

echo $md5Value $sha1Value $ARTIFACT_FILE_PATH


function publish() {
  echo "INFO: Uploading $ARTIFACT_FILE_PATH to $ARTIFACTORY_URL/$1"
  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "$ARTIFACT_FILE_PATH" \
   "$ARTIFACTORY_URL/$1"
}

WE_VERSION=`cat ../version.sbt | cut -d'=' -f 2 | xargs`

publish "system_tests-$SYSTEM_TESTS_TARGET-$WE_VERSION-$ARTIFACT_VERSION.zip"
publish "system_tests-$SYSTEM_TESTS_TARGET-$WE_VERSION-latest.zip"
