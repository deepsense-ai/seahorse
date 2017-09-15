#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

# This script is kept for backward jenkins compability. Eventually remove

cd `dirname $0`"/../"

./jenkins/publish_spark_docker.sh
./jenkins/publish_spark_docker_mesos.sh