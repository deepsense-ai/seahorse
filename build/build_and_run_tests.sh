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

# This is added here since sbt clean doesn't clean everything; in particular, it doesn't clean
# project/target, so we delete all "target". For discussion, see
# http://stackoverflow.com/questions/4483230/an-easy-way-to-get-rid-of-everything-generated-by-sbt
# and
# https://github.com/sbt/sbt/issues/896
find . -name target -type d -exec rm -rf {} \; || true

# Test that sdk's tests pass - it is not sbt submodule of deepsense-backend
(
  ./build/prepare_sdk_dependencies.sh
  cd seahorse-sdk-example
  sbt clean
  sbt test
)

run_tests() {
  sbt clean
  sbt -DSPARK_VERSION=$1 test ds-it
}
run_tests 2.2.0
run_tests 2.1.1
run_tests 2.0.2

(
  cd seahorse-workflow-executor
  ./build/build_and_run_tests.sh
)
