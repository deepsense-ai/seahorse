#!/bin/bash -x

# Copyright 2016, deepsense.ai
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


function prepare_environment() {
  echo "** Preparing Environment **"
  # this returns an error unless it's run for the first time on a machine
  # we know about it and want to continue anyway
  npmrc -c codilime || test 1
  npmrc codilime
  npm set registry $NPM_REGISTRY_URL

  sudo npm install -g webpack
  sudo npm install -g npmrc
  npm install
}

function build() {
  echo "** Building package **"
  npm run dist
}

prepare_environment
build
