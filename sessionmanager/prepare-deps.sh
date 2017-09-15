#!/bin/bash

if [ "$#" != "1" ] ; then
  echo "Usage: $0 <deepsense_backend_root>"
  exit 1
fi

PROJECT=`readlink -f $1` # absolute path
echo "Project dir: $PROJECT"
pwd
SM_DIR=$PROJECT/sessionmanager

TMPDIR=$SM_DIR/target/_we_deps_tmp

rm -Rf $TMPDIR
mkdir $TMPDIR

WE_DEPS_CONTENT=$TMPDIR/we_deps_target
mkdir $WE_DEPS_CONTENT

echo "Copying pyexecutor from workflowexecutor jar"

WE_JAR=$SM_DIR/target/downloads/we.jar
(cd $WE_DEPS_CONTENT; jar xf $WE_JAR pyexecutor)

echo "Copying executing kernel"
(cd $PROJECT/remote_notebook; ./pack_executing_kernel.sh)
unzip $PROJECT/remote_notebook/notebook_executing_kernel.zip -d $WE_DEPS_CONTENT

# Pyspark and Py4j from we-deps-consts
echo "Copying files from we-deps-consts files"
cp -R $SM_DIR/we-deps-consts/* $WE_DEPS_CONTENT

WE_DEPS_TARGET=$SM_DIR/target/we-deps.zip
rm -f $WE_DEPS_TARGET
(cd $WE_DEPS_CONTENT; zip -r we-deps.zip *)
mv $WE_DEPS_CONTENT/we-deps.zip $WE_DEPS_TARGET
rm -rf $TMPDIR
