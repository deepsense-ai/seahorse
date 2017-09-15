#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

# Set working directory to `seahorse-workflow-executor` submodule project
# `dirname $0` gives folder containing script
cd `dirname $0`"/../seahorse-workflow-executor"

# Import function for publishing to artifactory
source ../jenkins/publish_to_artifactory_function.sh

build_and_publish () {
    SPARK_VERSION=$1
    SCALA_VERSION=$2
    sbt -DsparkVersion=$SPARK_VERSION -Dsbt.log.noformat=true clean workflowexecutor/assembly
    publish_to_artifactory target/workflowexecutor.jar seahorse-distribution/io/deepsense/$SEAHORSE_BUILD_TAG/workflowexecutor/workflowexecutor-$SCALA_VERSION-$SPARK_VERSION.jar
}

build_and_publish "1.6.1" "2.10"
build_and_publish "2.0.0" "2.11"
build_and_publish "2.0.1" "2.11"
build_and_publish "2.0.2" "2.11"
build_and_publish "2.1.0" "2.11"
build_and_publish "2.1.1" "2.11"
