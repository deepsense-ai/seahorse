#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

# Exit script after first erroneous instruction
set -e

source /etc/profile
export HOME=/home/spark

# Prepare .bashrc
sed -i '1isource /etc/profile' /home/spark/.bashrc

cat >> /opt/spark/conf/spark-env.sh << EOF

export SPARK_MASTER_IP=${ENV_PREFIX}master
export SPARK_LOCAL_IP=$(hostname)
export SPARK_MASTER_OPTS="-Dspark.driver.port=7001 -Dspark.fileserver.port=7002
 -Dspark.broadcast.port=7003 -Dspark.replClassServer.port=7004
 -Dspark.blockManager.port=7005 -Dspark.executor.port=7006
 -Dspark.ui.port=4040 -Dspark.broadcast.factory=org.apache.spark.broadcast.HttpBroadcastFactory"
export SPARK_WORKER_OPTS="-Dspark.driver.port=7001 -Dspark.fileserver.port=7002
 -Dspark.broadcast.port=7003 -Dspark.replClassServer.port=7004
 -Dspark.blockManager.port=7005 -Dspark.executor.port=7006
 -Dspark.ui.port=4040 -Dspark.broadcast.factory=org.apache.spark.broadcast.HttpBroadcastFactory"
export SPARK_MASTER_PORT=7077
export SPARK_MASTER_WEBUI_PORT=8080
export SPARK_WORKER_PORT=8888
export SPARK_WORKER_WEBUI_PORT=8081

EOF

if [ "${IS_MASTER:-0}" == "1" ]
then

  # Scan ssh host keys
  ssh-keyscan -p 9022 0.0.0.0 >> /home/spark/.ssh/known_hosts
  ssh-keyscan -p 9022 ${ENV_PREFIX}master >> /home/spark/.ssh/known_hosts
  for i in `seq 1 ${NUMBER_OF_SLAVES:-0}`
  do
    ssh-keyscan -p 9022 "${ENV_PREFIX}slave$i" >> /home/spark/.ssh/known_hosts
  done

  # Prepare SPARK cluster configuration
  echo "${ENV_PREFIX}master" > /opt/spark/conf/slaves
  for i in `seq 1 ${NUMBER_OF_SLAVES:-0}`
  do
    echo "${ENV_PREFIX}slave$i" >> /opt/spark/conf/slaves
  done

  # Start services
  /opt/spark/sbin/start-all.sh
fi

# Do not halt the container
if [[ $1 == "-d" ]]
then
  while true; do sleep 1000; done
fi

if [[ $1 == "bash" ]]
then
  /bin/bash
fi
