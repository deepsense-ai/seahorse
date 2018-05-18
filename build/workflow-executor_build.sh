#!/bin/bash -ex
# Copyright 2017 deepsense.ai (CodiLime, Inc)
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
#

# Set working directory to `seahorse-workflow-executor` submodule project
# `dirname $0` gives folder containing script
cd `dirname $0`"/../seahorse-workflow-executor"


build_workflowexecutor () {
    SPARK_VERSION=$1
    sbt -DsparkVersion=$SPARK_VERSION -Dsbt.log.noformat=true clean workflowexecutor/assembly
}

build_workflowexecutor "2.0.0"
build_workflowexecutor "2.0.1"
build_workflowexecutor "2.0.2"
build_workflowexecutor "2.1.0"
build_workflowexecutor "2.1.1"
build_workflowexecutor "2.2.0"
