#!/usr/bin/env bash

cd `dirname $0`"/cluster-node-docker/"

docker build -t deepsense/docker-spark-standalone:local .