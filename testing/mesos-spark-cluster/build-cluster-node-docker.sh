#!/usr/bin/env bash
#
# Copyright (c) 2016, CodiLime Inc.
#

set -e

cd `dirname $0`

if [ $# -ne 2 ]; then
    echo "Usage: build-cluster-node-docker.sh SPARK_VERSION HADOOP_VERSION"
    exit 1
fi

SPARK_VERSION=$1
HADOOP_VERSION=$2

(cd cluster-node-docker/mesos-master; docker build --build-arg SPARK_VERSION=$SPARK_VERSION --build-arg HADOOP_VERSION=$HADOOP_VERSION -t deepsense_io/docker-mesos-master:local .)
(cd cluster-node-docker/mesos-slave; docker build -t deepsense_io/docker-mesos-slave:local .)
(cd cluster-node-docker/zookeeper; docker build -t deepsense_io/docker-zookeeper:local .)