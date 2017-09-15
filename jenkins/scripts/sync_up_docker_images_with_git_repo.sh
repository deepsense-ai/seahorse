#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

# This script is idempotent and lazy. You can safely call it from other scripts to ensure dockers are built.
cd `dirname $0`"/../../"

GIT_SHA=`git rev-parse HEAD`
# Without it SM dockers SbtGit.GitKeys.gitCurrentBranch will resolve to git hash instead of branch
# and fail with $GIT_SHA-latest doesnt exists.
# TODO Fix SM docker building process and get rid of SEAHORSE_BUILD_TAG here
export SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG:-$GIT_SHA}" # SET if SEAHORSE_BUILD_TAG not set

./jenkins/manage-docker.py --sync --publish --all
