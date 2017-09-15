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

cd `dirname $0`"/../"
ROOT=`pwd`

GIT_SHA=`git rev-parse HEAD`

SWE=$ROOT/seahorse-workflow-executor
DOCS=$SWE/docs
(
  cd $SWE
  sbt generateExamples
)
(
  cd $DOCS
  jekyll build
)
(
  cd deployment/documentation-docker
  cp -r $DOCS/_site .
  docker build -t "seahorse-documentation:$GIT_SHA" .
  rm -r _site
)
