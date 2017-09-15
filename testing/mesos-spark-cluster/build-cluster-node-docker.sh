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


set -e

cd `dirname $0`

if [ $# -ne 2 ]; then
    echo "Usage: build-cluster-node-docker.sh SPARK_VERSION HADOOP_VERSION"
    exit 1
fi

SPARK_VERSION=$1
HADOOP_VERSION=$2

(cd cluster-node-docker/mesos-master; docker build --build-arg SPARK_VERSION=$SPARK_VERSION --build-arg HADOOP_VERSION=$HADOOP_VERSION -t seahorse/docker-mesos-master:local .)
(cd cluster-node-docker/mesos-slave; docker build -t seahorse/docker-mesos-slave:local .)
(cd cluster-node-docker/zookeeper; docker build -t seahorse/docker-zookeeper:local .)
