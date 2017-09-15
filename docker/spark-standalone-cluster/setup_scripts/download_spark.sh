#!/bin/sh -ex
# Copyright (c) 2016, CodiLime Inc.

NAME=spark-2.0.0-bin-hadoop2.7
ARCHIVE_FILE=$NAME.tgz
TARGET_DIR=/opt

# download spark
wget http://archive.apache.org/dist/spark/spark-2.0.0/$ARCHIVE_FILE -O $ARCHIVE_FILE

# verify checksum
echo 3A1598EB7C32384830C48C779141C1C6  $ARCHIVE_FILE | md5sum -c -

# extract to /opt
tar -xvzf $ARCHIVE_FILE -C $TARGET_DIR

# create symbolic link
ln -s $TARGET_DIR/$NAME $TARGET_DIR/spark

# delete downloaded archive
rm $ARCHIVE_FILE
