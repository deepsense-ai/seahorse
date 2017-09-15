# Copyright (c) 2016, CodiLime Inc.

FROM ubuntu:16.04

ARG SPARK_VERSION
ARG HADOOP_VERSION

RUN apt-get -y update
RUN DEBIAN_FRONTEND=noninteractive apt-get install -y --force-yes software-properties-common python-software-properties
RUN apt-get install -y \
  openjdk-8-jdk \
  openjdk-8-jre \
  wget \
  vim \
  tmux \
  less \
  iputils-ping \
  supervisor \
  mc

# Install R packages
# gfortran liblapack-dev liblapack3 libopenblas-base libopenblas-dev are needed for machine learning R libraries
RUN apt-get update && \
   apt-get install -y build-essential openjdk-8-jre wget curl bzip2 openssh-server openssh-client libsm6 \
   r-base && \
   apt-get install -y gfortran liblapack-dev liblapack3 libopenblas-base libopenblas-dev && \
   apt-get clean && \
   apt-get autoremove && \
   rm -rf /var/lib/apt/lists/*

WORKDIR /opt/

COPY setup_scripts/ /opt/setup_scripts/
RUN /opt/setup_scripts/download_spark.sh ${SPARK_VERSION} ${HADOOP_VERSION}

COPY conf/ /opt/conf/

ENV SPARK_HOME /opt/spark

ENV SPARK_MASTER_OPTS "-Dspark.driver.port=7001 -Dspark.fileserver.port=7002 -Dspark.broadcast.port=7003 -Dspark.replClassServer.port=7004 -Dspark.blockManager.port=7005 -Dspark.executor.port=7006 -Dspark.ui.port=4040 -Dspark.broadcast.factory=org.apache.spark.broadcast.HttpBroadcastFactory"
ENV SPARK_WORKER_OPTS "-Dspark.driver.port=7001 -Dspark.fileserver.port=7002 -Dspark.broadcast.port=7003 -Dspark.replClassServer.port=7004 -Dspark.blockManager.port=7005 -Dspark.executor.port=7006 -Dspark.ui.port=4040 -Dspark.broadcast.factory=org.apache.spark.broadcast.HttpBroadcastFactory"

ENV SPARK_MASTER_PORT 7077
ENV SPARK_MASTER_WEBUI_PORT 8080
ENV SPARK_WORKER_PORT 8888
ENV SPARK_WORKER_WEBUI_PORT 8081

EXPOSE 8080 7077 8888 8081 4040 7001 7002 7003 7004 7005 7006
