#!/usr/bin/env bash
# Copyright (c) 2016, CodiLime Inc.

set -ex

cd `dirname $0`

docker build -t deepsense_io/docker-spark-standalone:local .
