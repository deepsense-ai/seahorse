#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.

# This is a helper for managing the dockerized standalone spark cluster.
# Its first parameter is up/down. In case of down, second parameter should be spark version.
# It's useful, because the docker-compose it manages requires an external network to exist.
# This script handles the lifecycle of said network.
# If there is a need to run multiple instances of the cluster,
# the CLUSTER_ID env should be set to random alphanumeric string.

set -ex

cd `dirname $0`

if [ "$CLUSTER_ID" == "" ]; then
  export CLUSTER_ID="SOME_ID"
fi

NETWORK_NAME="sbt-test-$CLUSTER_ID"



case $1 in
  up)
    SPARK_VERSION=$2
    export SPARK_VERSION=${SPARK_VERSION}
    if [ "$SPARK_VERSION" == "2.0.0" ]; then
      export HADOOP_VERSION="2.7.1"
    else
      export HADOOP_VERSION="2.6.0"
    fi
    docker network create --subnet=10.255.2.1/24 $NETWORK_NAME
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

