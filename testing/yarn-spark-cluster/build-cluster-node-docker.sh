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

CWD=`readlink -f $(dirname $0)`

cd "$CWD/cluster-node-docker/generic-hadoop-node"
docker build -t seahorse/generic-hadoop-node:local .

cd "$CWD/cluster-node-docker/yarn-master"
docker build -t seahorse/yarn-master:local .

cd "$CWD/cluster-node-docker/yarn-slave"
docker build -t seahorse/yarn-slave:local .
