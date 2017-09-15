#!/bin/bash -ex
# Copyright 2016, deepsense.ai
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

# Function for publishing artifacts to deepsense.ai internal artifactory
# Usage:
# source jenkins/publish_to_artifactory_function.sh
# publish_to_artifactory jenkins/test test-local/ai/deepsense/testing

function publish_to_artifactory {
  if [ "$#" -ne 2 ]; then
    echo "Usage: publish PATH_TO_FILE PATH_IN_ARTIFACTORY"
    echo "This will publish \$PATH_TO_FILE to http://artifactory.deepsense.codilime.com:8081/artifactory/\$PATH_IN_ARTIFACTORY"
    exit 1
  fi

  local ARTIFACTORY_CREDENTIALS=$HOME/.artifactory_credentials
  local ARTIFACTORY_USER=`grep "user=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
  local ARTIFACTORY_PASSWORD=`grep "password=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`
  local ARTIFACTORY_URL=`grep "host=" $ARTIFACTORY_CREDENTIALS | cut -d '=' -f 2`

  local LOCAL_ARTIFACT_LOCATION=$1
  local REMOTE_ARTIFACT_LOCATION=$ARTIFACTORY_URL/$2

  echo "** INFO: Uploading $LOCAL_ARTIFACT_LOCATION to ${REMOTE_ARTIFACT_LOCATION} **"
  local md5Value="`md5sum "${LOCAL_ARTIFACT_LOCATION}"`"
  local md5Value="${md5Value:0:32}"
  local sha1Value="`sha1sum "${LOCAL_ARTIFACT_LOCATION}"`"
  local sha1Value="${sha1Value:0:40}"

  curl -i -X PUT -u $ARTIFACTORY_USER:$ARTIFACTORY_PASSWORD \
   -H "X-Checksum-Md5: $md5Value" \
   -H "X-Checksum-Sha1: $sha1Value" \
   -T "${LOCAL_ARTIFACT_LOCATION}" \
   "${REMOTE_ARTIFACT_LOCATION}"
}
