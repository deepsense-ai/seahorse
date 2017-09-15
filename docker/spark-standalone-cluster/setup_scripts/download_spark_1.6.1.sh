#!/bin/sh -ex
# Copyright (c) 2016, CodiLime Inc.

NAME=spark-1.6.1-bin-hadoop2.6

ARCHIVE_FILE=$NAME.tgz
TARGET_DIR=/opt

# download spark
wget http://archive.apache.org/dist/spark/spark-1.6.1/$ARCHIVE_FILE -O $ARCHIVE_FILE

# verify checksum
echo 667A62D7F289479A19DA4B563E7151D4  $ARCHIVE_FILE | md5sum -c -

# extract to /opt
tar -xvzf $ARCHIVE_FILE -C $TARGET_DIR

# create symbolic link
ln -s $TARGET_DIR/$NAME $TARGET_DIR/spark

# delete downloaded archive
rm $ARCHIVE_FILE
