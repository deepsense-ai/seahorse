#!/usr/bin/env bash
#
# Copyright (c) 2016, CodiLime Inc.
#

set -e

cd `dirname $0`

(cd cluster-node-docker/mesos-master; docker build -t deepsense_io/docker-mesos-master:local .)
(cd cluster-node-docker/mesos-slave; docker build -t deepsense_io/docker-mesos-slave:local .)
(cd cluster-node-docker/zookeeper; docker build -t deepsense_io/docker-zookeeper:local .)