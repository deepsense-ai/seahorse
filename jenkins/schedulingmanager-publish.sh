#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.
#
# Build and publish docker

cd `dirname $0`"/../"

sbt clean schedulingmanager/docker:publishLocal

cd deployment/docker
./publish-local-docker.sh deepsense-schedulingmanager
