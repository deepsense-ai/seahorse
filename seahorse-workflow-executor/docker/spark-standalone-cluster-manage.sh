#!/usr/bin/env bash
# Copyright 2016 deepsense.ai (CodiLime, Inc)
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


# This is a helper for managing the dockerized standalone spark cluster.
# It receives two parameters: ACTION (up/down) and SPARK_VERSION.
# It's useful, because the docker-compose it manages requires an external network to exist.
# This script handles the lifecycle of said network.
# If there is a need to run multiple instances of the cluster,
# the CLUSTER_ID env should be set to random alphanumeric string.

set -ex

cd `dirname $0`

if [ "$CLUSTER_ID" == "" ]; then
  export CLUSTER_ID="SOME_ID"
fi

if [ "$NETWORK_NAME" == "" ]; then
  export NETWORK_NAME="sbt-test-$CLUSTER_ID"
fi

function networkCreate {
  networkName=$1
  for subnet in `seq 5 255`; do
    set +e
    docker network create --subnet=10.255.$subnet.1/24 $networkName
    created=$?
    set -e

    if [ $created == 0 ]; then
      break
    fi
  done
}

function networkRm {
  networkName=$1
  docker network rm $1 || : # this fails if the network doesn't exist
}

ACTION=$1
SPARK_VERSION=$2
export SPARK_VERSION=${SPARK_VERSION}
if  [ "$SPARK_VERSION" == "2.1.0" ] || [ "$SPARK_VERSION" == "2.1.1" ] || [ "$SPARK_VERSION" == "2.2.0" ]; then
  export HADOOP_VERSION="2.7"
  # We use 2.7.1 for Spark 2.1.x despite the fact that the latter depends on 2.7.3, but 2.7.3
  # doesn't have docker image released yet.
  export HADOOP_VERSION_FULL="2.7.1"
elif [ "$SPARK_VERSION" == "2.0.0" ] || [ "$SPARK_VERSION" == "2.0.1" ] || [ "$SPARK_VERSION" == "2.0.2" ]; then
  export HADOOP_VERSION="2.7"
  export HADOOP_VERSION_FULL="2.7.1"
else
  echo 'Unhandled Spark version ${$SPARK_VERSION}'
  exit 1
fi

case $ACTION in
  up)
    spark-standalone-cluster/build-cluster-node-docker.sh $SPARK_VERSION $HADOOP_VERSION
    networkRm $NETWORK_NAME
    networkCreate $NETWORK_NAME
    docker-compose -f spark-standalone-cluster.dc.yml up -d
    ;;
  down)
    docker-compose -f spark-standalone-cluster.dc.yml kill
    docker-compose -f spark-standalone-cluster.dc.yml down
    docker network rm $NETWORK_NAME || : # this may fail when down if called before up
    ;;
  *)
    echo 'Unknown action!'
    exit 1
    ;;
esac
