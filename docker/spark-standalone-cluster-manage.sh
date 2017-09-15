#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.

# This is a helper for managing the dockerized standalone spark cluster.
# It accepts a single string parameter: up|down
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

function networkCreate {
  networkName=$1
  for subnet in `seq 2 255`; do
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

case $1 in
  up)
    spark-standalone-cluster/build-cluster-node-docker.sh
    networkRm $NETWORK_NAME
    networkCreate $NETWORK_NAME
    docker-compose -f spark-standalone-cluster.dc.yml up -d
    ;;
  down)
    docker-compose -f spark-standalone-cluster.dc.yml kill
    docker-compose -f spark-standalone-cluster.dc.yml down
    networkRm $NETWORK_NAME
    ;;
  *)
    echo 'Unknown action!'
    exit 1
    ;;
esac

