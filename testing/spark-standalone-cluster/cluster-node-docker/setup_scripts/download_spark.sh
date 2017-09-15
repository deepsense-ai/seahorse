#!/bin/sh

NAME=spark-2.0.0-bin-hadoop2.7
ARCHIVE_FILE=$NAME.tgz
TARGET_DIR=/opt

# download spark
wget http://www.apache.org/dist/spark/spark-2.0.0/$ARCHIVE_FILE -O $ARCHIVE_FILE || exit 1

# verify checksum
echo 3A1598EB7C32384830C48C779141C1C6  $ARCHIVE_FILE | md5sum -c - || exit 1

# extract to /opt
tar -xvzf $ARCHIVE_FILE -C $TARGET_DIR || exit 1

# create symbolic link
ln -s $TARGET_DIR/$NAME $TARGET_DIR/spark || exit 1

# delete downloaded archive
rm $ARCHIVE_FILE || exit 1