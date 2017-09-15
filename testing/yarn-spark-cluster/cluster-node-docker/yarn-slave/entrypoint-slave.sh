#!/bin/bash
# Copyright 2016 deepsense.ai (CodiLime, Inc)
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.


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
