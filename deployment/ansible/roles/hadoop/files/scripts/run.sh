#!/bin/bash

# Copyright (c) 2015, CodiLime Inc.

# Exit script after first erroneous instruction
set -e

source /etc/profile

# Prepare .bashrc
sed -i '1isource /etc/profile' /home/hadoop/.bashrc

if [ "${IS_MASTER:-1}" == "1" ] ; then

    # Scan ssh host keys
    ssh-keyscan -p 8022 0.0.0.0 >> /home/hadoop/.ssh/known_hosts
    ssh-keyscan -p 8022 ${ENV_PREFIX}master >> /home/hadoop/.ssh/known_hosts
    for i in `seq 1 ${NUMBER_OF_SLAVES:-0}`
    do
        ssh-keyscan -p 8022 "${ENV_PREFIX}slave$i" >> /home/hadoop/.ssh/known_hosts
    done

    # Prepare hadoop conf
    echo "${ENV_PREFIX}master" > /opt/hadoop/etc/hadoop/masters
    echo "${ENV_PREFIX}master" > /opt/hadoop/etc/hadoop/slaves
    for i in `seq 1 ${NUMBER_OF_SLAVES:-0}`
    do
        echo "${ENV_PREFIX}slave$i" >> /opt/hadoop/etc/hadoop/slaves
    done

    sed -e s/MASTER/${ENV_PREFIX}master/ /opt/hadoop/etc/hadoop/core-site.xml.template > /opt/hadoop/etc/hadoop/core-site.xml
    sed -e s/DFS_REPLICATION/${DFS_REPLICATION:-1}/ /opt/hadoop/etc/hadoop/hdfs-site.xml.template > /opt/hadoop/etc/hadoop/hdfs-site.xml
    sed -e s/RM_HOSTNAME/0.0.0.0/ -e s/MASTER/${ENV_PREFIX}master/ -e s/YARN_MEMORY/${YARN_MEMORY:-2650}/ /opt/hadoop/etc/hadoop/yarn-site.xml.template > /opt/hadoop/etc/hadoop/yarn-site.xml

    # Format namenode (if not formatted)
    if [ ! -d "/hdfs/dfs/name" ]
    then
      /opt/hadoop/bin/hadoop namenode -format
    fi

    # Start services
    /opt/hadoop/sbin/start-all.sh

    # Wait for HDFS
    while ! /opt/hadoop/bin/hadoop fs -test -d /
    do
      echo "Waiting for HDFS..."
      sleep 1
    done

    # Create spark tmp directory
    if ! /opt/hadoop/bin/hadoop fs -test -d /tmp/spark-events
    then
      /opt/hadoop/bin/hadoop fs -mkdir -p /tmp/spark-events
    fi

else

    # Prepare hadoop conf
    sed -e s/MASTER/${ENV_PREFIX}master/ /opt/hadoop/etc/hadoop/core-site.xml.template > /opt/hadoop/etc/hadoop/core-site.xml
    sed -e s/DFS_REPLICATION/${DFS_REPLICATION:-1}/ /opt/hadoop/etc/hadoop/hdfs-site.xml.template > /opt/hadoop/etc/hadoop/hdfs-site.xml
    sed -e s/RM_HOSTNAME/${ENV_PREFIX}master/ -e s/MASTER/${ENV_PREFIX}master/ -e s/YARN_MEMORY/${YARN_MEMORY:-2650}/ /opt/hadoop/etc/hadoop/yarn-site.xml.template > /opt/hadoop/etc/hadoop/yarn-site.xml
fi

# Do not halt the container
if [[ $1 == "-d" ]]; then
  while true; do sleep 1000; done
fi

if [[ $1 == "bash" ]]; then
  /bin/bash
fi
