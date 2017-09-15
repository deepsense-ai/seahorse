#!/bin/bash -ex
# Copyright (c) 2016, CodiLime Inc.

SEAHORSE_BUILD_TAG="${SEAHORSE_BUILD_TAG?Need to set SEAHORSE_BUILD_TAG. For example export SEAHORSE_BUILD_TAG=SEAHORSE_BUILD_TAG=\`date +%Y%m%d_%H%M%S\`-\$GIT_TAG}"

# Set working directory to `seahorse-workflow-executor` submodule project
# `dirname $0` gives folder containing script
cd `dirname $0`"/../seahorse-workflow-executor"

# Import function for publishing to artifactory
source ../jenkins/publish_to_artifactory_function.sh


# Build and publish to artifactory workflowexecutor.jar for Spark 1.6.1 and Scala 2.10 ONLY
SPARK_VERSION=1.6.1
SCALA_VERSION=2.10
SBT_OPTS="-XX:MaxPermSize=4G" sbt -DsparkVersion=$SPARK_VERSION -Dsbt.log.noformat=true clean workflowexecutor/assembly
publish_to_artifactory target/workflowexecutor.jar seahorse-distribution/io/deepsense/$SEAHORSE_BUILD_TAG/workflowexecutor/workflowexecutor-$SCALA_VERSION-$SPARK_VERSION.jar

# Build and publish to artifactory workflowexecutor.jar for Spark 2.0.0 and Scala 2.11 ONLY
SPARK_VERSION=2.0.0
SCALA_VERSION=2.11
SBT_OPTS="-XX:MaxPermSize=4G" sbt -DsparkVersion=$SPARK_VERSION -Dsbt.log.noformat=true clean workflowexecutor/assembly
publish_to_artifactory target/workflowexecutor.jar seahorse-distribution/io/deepsense/$SEAHORSE_BUILD_TAG/workflowexecutor/workflowexecutor-$SCALA_VERSION-$SPARK_VERSION.jar
