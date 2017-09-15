#!/bin/bash
#
# Copyright (c) 2016, CodiLime Inc.
#

echo "export PATH=$PATH:/opt/conda/bin" >> /data/.bash_profile
chown ${HDFS_USER}:${HDFS_USER} /data/.bash_profile

echo "Starting HDFS datanode"
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/hadoop-daemon.sh start datanode
if [ $? -ne 0 ]; then
  echo "Error during HDFS datanode start! Exiting..."
  exit 1
fi
echo "HDFS datanode started"

echo "Starting YARN node manager"
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/yarn-daemon.sh start nodemanager
if [ $? -ne 0 ]; then
  echo "Error during YARN node manager start! Exiting..."
  exit 1
fi
echo "YARN node manager started"

while true; do sleep 1000; done
