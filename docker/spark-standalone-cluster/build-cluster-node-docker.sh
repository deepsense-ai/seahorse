#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.

set -ex

cd `dirname $0`

SPARK_VERSION=$1

docker build --build-arg SPARK_VERSION=${SPARK_VERSION} -t deepsense_io/docker-spark-standalone-${SPARK_VERSION}:local .
