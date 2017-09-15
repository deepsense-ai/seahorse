#!/bin/bash

source /etc/profile

# Clean HDFS dirs
hadoop fs -rm -r -f {{ge_hdfs_dir}}/lib/graphexecutor.jar
hadoop fs -rm -r -f {{ge_hdfs_dir}}/lib/graphexecutor-deps.jar
hadoop fs -rm -r -f {{ge_hdfs_dir}}/etc/application.conf
hadoop fs -rm -r -f {{ge_hdfs_dir}}/etc/log4j.xml

# Prepare HDFS dirs
hadoop fs -mkdir -p {{ge_hdfs_dir}}/data
hadoop fs -mkdir -p {{ge_hdfs_dir}}/etc
hadoop fs -mkdir -p {{ge_hdfs_dir}}/lib
hadoop fs -mkdir -p {{ge_hdfs_dir}}/tmp
hadoop fs -mkdir -p {{ge_hdfs_dir}}/var/log

# Copy to HDFS
hadoop fs -put {{ge_install_dir}}/deepsense-graphexecutor/lib/deepsense-graphexecutor-assembly-{{ge_version}}.jar {{ge_hdfs_dir}}/lib/graphexecutor.jar
hadoop fs -put {{ge_install_dir}}/deepsense-graphexecutor/lib/deepsense-graphexecutor-assembly-{{ge_version}}-deps.jar {{ge_hdfs_dir}}/lib/graphexecutor-deps.jar
hadoop fs -put {{ge_install_dir}}/application.conf {{ge_hdfs_dir}}/etc/application.conf
hadoop fs -put {{ge_install_dir}}/log4j.xml {{ge_hdfs_dir}}/etc/log4j.xml
