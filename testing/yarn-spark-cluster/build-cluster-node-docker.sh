#!/usr/bin/env bash
#
# Copyright (c) 2016, CodiLime Inc.
#

set -e

CWD=`readlink -f $(dirname $0)`

cd "$CWD/cluster-node-docker/generic-hadoop-node"
docker build -t deepsense_io/generic-hadoop-node:local .

cd "$CWD/cluster-node-docker/yarn-master"
docker build -t deepsense_io/yarn-master:local .

cd "$CWD/cluster-node-docker/yarn-slave"
docker build -t deepsense_io/yarn-slave:local .
