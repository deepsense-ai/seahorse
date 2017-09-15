#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.

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
if [ "$SPARK_VERSION" == "2.0.0" ] || [ "$SPARK_VERSION" == "2.0.1" ] || [ "$SPARK_VERSION" == "2.0.2" ]; then
  export HADOOP_VERSION="2.7"
  export HADOOP_VERSION_FULL="2.7.1"
elif [ "$SPARK_VERSION" == "1.6.1" ]; then
  export HADOOP_VERSION="2.6"
  export HADOOP_VERSION_FULL="2.6.0"
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
