#!/bin/bash

echo 'This script assumes its run from root of deepsense backend project'

SM_DIR=sessionmanager

TMPDIR=$SM_DIR/target/_we_deps_tmp

rm -Rf "$TMPDIR"
mkdir "$TMPDIR"

WE_DEPS_CONTENT=$TMPDIR/we_deps_target
mkdir $WE_DEPS_CONTENT

echo "Copying pyexecutor from workflowexecutor jar"

WE_JAR="../../../../seahorse-workflow-executor/workflowexecutor/target/scala-2.11/workflowexecutor.jar"
(cd $WE_DEPS_CONTENT; jar xf $WE_JAR pyexecutor)
(cd $WE_DEPS_CONTENT; jar xf $WE_JAR r_executor.R)

echo "Copying executing kernel"
(cd remote_notebook; ./pack_executing_kernel.sh)
unzip remote_notebook/notebook_executing_kernel.zip -d $WE_DEPS_CONTENT

# Pyspark and Py4j from we-deps-consts
# TODO: We should not include this files, but take Pyspark and Py4j from Spark installed on cluster
echo "Copying files from we-deps-consts files"
cp -R $SM_DIR/we-deps-consts/* $WE_DEPS_CONTENT

WE_DEPS_TARGET="$SM_DIR/target/we-deps.zip"
rm -f $WE_DEPS_TARGET
(cd $WE_DEPS_CONTENT; zip -r we-deps.zip *)
mv $WE_DEPS_CONTENT/we-deps.zip $WE_DEPS_TARGET
rm -rf $TMPDIR
