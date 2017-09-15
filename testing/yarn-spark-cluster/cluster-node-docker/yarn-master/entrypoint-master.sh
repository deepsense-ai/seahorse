#!/bin/bash
#
# Copyright (c) 2016, CodiLime Inc.
#

echo "export PATH=$PATH:/opt/conda/bin" >> /data/.bash_profile
chown ${HDFS_USER}:${HDFS_USER} /data/.bash_profile

echo "Formatting HDFS namenode..."
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/bin/hdfs namenode -format -nonInteractive
if [ $? -ne 0 ]; then
  echo "Error during HDFS namenode format! Exiting..."
  exit 1
fi
echo "HDFS namenode formatted"

echo "Starting HDFS namenode..."
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/hadoop-daemon.sh start namenode
if [ $? -ne 0 ]; then
  echo "Error during HDFS namenode start! Exiting..."
  exit 1
fi
echo "HDFS namenode started"

echo "Starting HDFS datanode"
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/hadoop-daemon.sh start datanode
if [ $? -ne 0 ]; then
  echo "Error during HDFS datanode start! Exiting..."
  exit 1
fi
echo "HDFS datanode started"

echo "Starting YARN resource manager"
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/yarn-daemon.sh start resourcemanager
if [ $? -ne 0 ]; then
  echo "Error during YARN resource manager start! Exiting..."
  exit 1
fi
echo "YARN resource manager started"

echo "Starting YARN node manager"
sudo -u ${HDFS_USER} -E ${HADOOP_HOME}/sbin/yarn-daemon.sh start nodemanager
if [ $? -ne 0 ]; then
  echo "Error during YARN node manager start! Exiting..."
  exit 1
fi
echo "YARN node manager started"

while true; do sleep 1000; done
