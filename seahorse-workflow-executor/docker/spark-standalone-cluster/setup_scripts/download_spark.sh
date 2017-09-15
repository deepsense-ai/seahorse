#!/bin/sh -ex
# Copyright (c) 2016, CodiLime Inc.

SPARK_VERSION=$1
HADOOP_VERSION=$2

NAME=spark-$SPARK_VERSION-bin-hadoop$HADOOP_VERSION

ARCHIVE_FILE=$NAME.tgz
TARGET_DIR=/opt

# download spark
wget http://archive.apache.org/dist/spark/spark-$SPARK_VERSION/$ARCHIVE_FILE

# extract to /opt
tar -xvzf $ARCHIVE_FILE -C $TARGET_DIR

# create symbolic link
ln -s $TARGET_DIR/$NAME $TARGET_DIR/spark

# delete downloaded archive
rm $ARCHIVE_FILE
