#!/bin/bash -ex
# This is added here since sbt clean doesn't clean everything; in particular, it doesn't clean
# project/target, so we delete all "target". For discussion, see
# http://stackoverflow.com/questions/4483230/an-easy-way-to-get-rid-of-everything-generated-by-sbt
# and
# https://github.com/sbt/sbt/issues/896
find . -name target -type d -exec rm -rf {} \; || true

# Test that sdk's tests pass - it is not sbt submodule of deepsense-backend
(
  ./jenkins/prepare_sdk_dependencies.sh
  cd seahorse-sdk-example
  sbt clean
  sbt test
)

run_tests() {
    sbt clean
    sbt -DSPARK_VERSION=$1 scalastylebackend test ds-it
}
run_tests 2.1.1
run_tests 2.0.2
run_tests 1.6.1
