#!/bin/bash

echo 'This script assumes its run from root of deepsense backend project'

WE_SPARK_VERSION=$1

SM_DIR=sessionmanager

TMPDIR=$SM_DIR/target/_we_deps_tmp

rm -Rf "$TMPDIR"
mkdir "$TMPDIR"

WE_DEPS_CONTENT=$TMPDIR/we_deps_target
mkdir $WE_DEPS_CONTENT

echo "Copying python and R executors from workflowexecutor jar"

WE_JAR="../../../../seahorse-workflow-executor/target/workflowexecutor.jar"
(cd $WE_DEPS_CONTENT; jar xf $WE_JAR pyexecutor)
(cd $WE_DEPS_CONTENT; jar xf $WE_JAR r_executor.R)

echo "Copying executing kernel"
(cd remote_notebook; ./pack_executing_kernel.sh)
unzip remote_notebook/notebook_executing_kernel.zip -d $WE_DEPS_CONTENT

# Pyspark and Py4j from we-deps-consts
# TODO: We should not include this files, but take Pyspark and Py4j from Spark installed on cluster
WE_DEPS_CONSTS=we-deps-consts/$WE_SPARK_VERSION
echo "Copying files from $WE_DEPS_CONSTS files"
cp -R $SM_DIR/$WE_DEPS_CONSTS/* $WE_DEPS_CONTENT

WE_DEPS_TARGET="$SM_DIR/target/we-deps.zip"
rm -f $WE_DEPS_TARGET
(cd $WE_DEPS_CONTENT; zip -r we-deps.zip *)
mv $WE_DEPS_CONTENT/we-deps.zip $WE_DEPS_TARGET
rm -rf $TMPDIR
